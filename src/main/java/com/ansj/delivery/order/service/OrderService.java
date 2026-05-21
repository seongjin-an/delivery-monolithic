package com.ansj.delivery.order.service;

import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.order.domain.OrderItem;
import com.ansj.delivery.order.domain.OrderItemOption;
import com.ansj.delivery.order.domain.OrderStatus;
import com.ansj.delivery.order.dto.*;
import com.ansj.delivery.order.repository.OrderRepository;
import com.ansj.delivery.restaurant.domain.Menu;
import com.ansj.delivery.restaurant.domain.MenuOptionItem;
import com.ansj.delivery.restaurant.domain.Restaurant;
import com.ansj.delivery.restaurant.domain.RestaurantStatus;
import com.ansj.delivery.restaurant.repository.MenuOptionItemRepository;
import com.ansj.delivery.restaurant.repository.MenuRepository;
import com.ansj.delivery.restaurant.repository.RestaurantRepository;
import com.ansj.delivery.notification.service.NotificationService;
import com.ansj.delivery.user.domain.User;
import com.ansj.delivery.user.repository.UserRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionItemRepository menuOptionItemRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse createOrder(UUID customerId, CreateOrderRequest request) {
        User customer = findUser(customerId);
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> BusinessException.notFound("가게를 찾을 수 없습니다."));

        if (restaurant.getStatus() != RestaurantStatus.OPEN) {
            throw BusinessException.badRequest("현재 영업 중인 가게가 아닙니다.");
        }

        // 1단계: 가격 계산 및 유효성 검증
        int itemsTotal = 0;
        List<PreparedItem> preparedItems = new ArrayList<>();

        for (CreateOrderItemRequest itemReq : request.items()) {
            Menu menu = menuRepository.findById(itemReq.menuId())
                    .orElseThrow(() -> BusinessException.notFound("메뉴를 찾을 수 없습니다."));

            if (!menu.isAvailable()) {
                throw BusinessException.badRequest(menu.getName() + " 메뉴는 현재 판매 중지 상태입니다.");
            }
            if (!menu.getRestaurant().getId().equals(restaurant.getId())) {
                throw BusinessException.badRequest("해당 메뉴는 이 가게의 메뉴가 아닙니다.");
            }

            int optionExtras = 0;
            List<MenuOptionItem> optionItems = new ArrayList<>();

            if (itemReq.selectedOptionItemIds() != null) {
                for (Long optionItemId : itemReq.selectedOptionItemIds()) {
                    MenuOptionItem optionItem = menuOptionItemRepository.findById(optionItemId)
                            .orElseThrow(() -> BusinessException.notFound("선택한 옵션을 찾을 수 없습니다."));
                    if (!optionItem.getMenuOption().getMenu().getId().equals(menu.getId())) {
                        throw BusinessException.badRequest("선택한 옵션이 해당 메뉴의 옵션이 아닙니다.");
                    }
                    optionExtras += optionItem.getExtraPrice();
                    optionItems.add(optionItem);
                }
            }

            int unitPrice = menu.getPrice() + optionExtras;
            itemsTotal += unitPrice * itemReq.quantity();
            preparedItems.add(new PreparedItem(menu, itemReq.quantity(), unitPrice, optionItems));
        }

        if (itemsTotal < restaurant.getMinOrderAmount()) {
            throw BusinessException.badRequest(
                    "최소 주문 금액은 " + restaurant.getMinOrderAmount() + "원입니다.");
        }

        int totalAmount = itemsTotal + restaurant.getDeliveryFee();

        // 2단계: 엔티티 생성
        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .totalAmount(totalAmount)
                .deliveryFee(restaurant.getDeliveryFee())
                .deliveryAddress(request.deliveryAddress())
                .deliveryLatitude(request.deliveryLatitude())
                .deliveryLongitude(request.deliveryLongitude())
                .requestNote(request.requestNote())
                .build();

        for (PreparedItem prep : preparedItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menu(prep.menu())
                    .menuName(prep.menu().getName())
                    .quantity(prep.quantity())
                    .unitPrice(prep.unitPrice())
                    .build();

            for (MenuOptionItem optionItem : prep.optionItems()) {
                OrderItemOption option = OrderItemOption.builder()
                        .orderItem(orderItem)
                        .optionName(optionItem.getMenuOption().getName())
                        .optionItemName(optionItem.getName())
                        .extraPrice(optionItem.getExtraPrice())
                        .build();
                orderItem.getOptions().add(option);
            }
            order.getItems().add(orderItem);
        }

        orderRepository.save(order);

        notificationService.send(
                restaurant.getOwner(), "ORDER_PLACED",
                "새 주문이 들어왔습니다",
                restaurant.getName() + "에 새 주문이 접수되었습니다.");

        return OrderResponse.from(order);
    }

    public List<OrderResponse> getMyOrders(UUID customerId) {
        User customer = findUser(customerId);
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer).stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse getOrder(UUID customerId, UUID orderId) {
        Order order = findOrder(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw BusinessException.forbidden("해당 주문에 대한 권한이 없습니다.");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID customerId, UUID orderId, CancelOrderRequest request) {
        Order order = findOrder(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw BusinessException.forbidden("해당 주문에 대한 권한이 없습니다.");
        }
        order.cancel(request.reason());

        notificationService.send(
                order.getRestaurant().getOwner(), "ORDER_CANCELLED",
                "주문이 취소되었습니다",
                order.getRestaurant().getName() + "의 주문이 취소되었습니다.");

        return OrderResponse.from(order);
    }

    // 사장님 전용
    public List<OrderResponse> getRestaurantOrders(UUID ownerId, Long restaurantId, OrderStatus status) {
        Restaurant restaurant = getOwnedRestaurant(ownerId, restaurantId);
        List<Order> orders = (status != null)
                ? orderRepository.findByRestaurantAndStatusOrderByCreatedAtDesc(restaurant, status)
                : orderRepository.findByRestaurantOrderByCreatedAtDesc(restaurant);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse acceptOrder(UUID ownerId, UUID orderId) {
        Order order = getOwnedOrder(ownerId, orderId);
        order.accept();

        notificationService.send(
                order.getCustomer(), "ORDER_ACCEPTED",
                "주문이 수락되었습니다",
                order.getRestaurant().getName() + "에서 주문을 수락했습니다.");

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse markReady(UUID ownerId, UUID orderId) {
        Order order = getOwnedOrder(ownerId, orderId);
        order.readyForPickup();
        return OrderResponse.from(order);
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.notFound("주문을 찾을 수 없습니다."));
    }

    private Order getOwnedOrder(UUID ownerId, UUID orderId) {
        Order order = findOrder(orderId);
        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("해당 주문에 대한 권한이 없습니다.");
        }
        return order;
    }

    private Restaurant getOwnedRestaurant(UUID ownerId, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> BusinessException.notFound("가게를 찾을 수 없습니다."));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("해당 가게에 대한 권한이 없습니다.");
        }
        return restaurant;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));
    }
}
