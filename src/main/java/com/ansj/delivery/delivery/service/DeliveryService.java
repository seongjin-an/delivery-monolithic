package com.ansj.delivery.delivery.service;

import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.delivery.domain.Delivery;
import com.ansj.delivery.delivery.domain.DeliveryStatus;
import com.ansj.delivery.delivery.domain.RiderLocation;
import com.ansj.delivery.delivery.dto.AvailableDeliveryResponse;
import com.ansj.delivery.delivery.dto.DeliveryResponse;
import com.ansj.delivery.delivery.dto.RiderLocationRequest;
import com.ansj.delivery.delivery.repository.DeliveryRepository;
import com.ansj.delivery.delivery.repository.RiderLocationRepository;
import com.ansj.delivery.notification.service.NotificationService;
import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.order.domain.OrderStatus;
import com.ansj.delivery.order.repository.OrderRepository;
import com.ansj.delivery.user.domain.User;
import com.ansj.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RiderLocationRepository riderLocationRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<AvailableDeliveryResponse> getAvailableDeliveries() {
        return orderRepository.findAvailableForDelivery().stream()
                .map(AvailableDeliveryResponse::from)
                .toList();
    }

    public List<DeliveryResponse> getMyDeliveries(UUID riderId, DeliveryStatus status) {
        User rider = findUser(riderId);
        List<Delivery> deliveries = (status != null)
                ? deliveryRepository.findByRiderAndStatusOrderByAssignedAtDesc(rider, status)
                : deliveryRepository.findByRiderOrderByAssignedAtDesc(rider);
        return deliveries.stream().map(DeliveryResponse::from).toList();
    }

    public DeliveryResponse getDeliveryByOrder(UUID orderId) {
        Order order = findOrder(orderId);
        Delivery delivery = deliveryRepository.findByOrder(order)
                .orElseThrow(() -> BusinessException.notFound("배달 정보를 찾을 수 없습니다."));
        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse acceptDelivery(UUID riderId, UUID orderId) {
        User rider = findUser(riderId);
        Order order = findOrder(orderId);

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw BusinessException.badRequest("픽업 준비 완료 상태의 주문만 수락할 수 있습니다.");
        }
        if (deliveryRepository.findByOrder(order).isPresent()) {
            throw BusinessException.conflict("이미 배달이 배정된 주문입니다.");
        }

        Delivery delivery = Delivery.builder()
                .order(order)
                .rider(rider)
                .build();

        DeliveryResponse response = DeliveryResponse.from(deliveryRepository.save(delivery));

        notificationService.send(
                order.getCustomer(), "DELIVERY_ASSIGNED",
                "배달기사가 배정되었습니다",
                rider.getName() + " 기사님이 배달을 맡았습니다.");

        return response;
    }

    @Transactional
    public DeliveryResponse pickup(UUID riderId, Long deliveryId) {
        Delivery delivery = getOwnedDelivery(riderId, deliveryId);

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw BusinessException.badRequest("배정 상태의 배달만 픽업 처리할 수 있습니다.");
        }

        delivery.pickup();
        delivery.getOrder().pickup();

        notificationService.send(
                delivery.getOrder().getCustomer(), "DELIVERY_PICKED_UP",
                "배달기사가 픽업했습니다",
                "곧 배달이 시작됩니다.");

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public DeliveryResponse complete(UUID riderId, Long deliveryId) {
        Delivery delivery = getOwnedDelivery(riderId, deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw BusinessException.badRequest("픽업 상태의 배달만 완료 처리할 수 있습니다.");
        }

        delivery.complete();
        delivery.getOrder().complete();

        notificationService.send(
                delivery.getOrder().getCustomer(), "DELIVERY_COMPLETED",
                "배달이 완료되었습니다",
                "주문하신 음식이 도착했습니다. 맛있게 드세요!");

        return DeliveryResponse.from(delivery);
    }

    @Transactional
    public void updateLocation(UUID riderId, RiderLocationRequest request) {
        User rider = findUser(riderId);
        RiderLocation location = RiderLocation.builder()
                .rider(rider)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        riderLocationRepository.save(location);
    }

    private Delivery getOwnedDelivery(UUID riderId, Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> BusinessException.notFound("배달 정보를 찾을 수 없습니다."));
        if (!delivery.getRider().getId().equals(riderId)) {
            throw BusinessException.forbidden("해당 배달에 대한 권한이 없습니다.");
        }
        return delivery;
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> BusinessException.notFound("주문을 찾을 수 없습니다."));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));
    }
}
