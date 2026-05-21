package com.ansj.delivery.restaurant.service;

import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.restaurant.domain.*;
import com.ansj.delivery.restaurant.dto.*;
import com.ansj.delivery.restaurant.repository.MenuRepository;
import com.ansj.delivery.restaurant.repository.RestaurantRepository;
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
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    @Transactional
    public RestaurantResponse create(UUID ownerId, CreateRestaurantRequest request) {
        User owner = findUser(ownerId);
        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(request.name())
                .phone(request.phone())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .category(request.category())
                .minOrderAmount(request.minOrderAmount())
                .deliveryFee(request.deliveryFee())
                .estimatedDeliveryMinutes(request.estimatedDeliveryMinutes())
                .build();
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse update(UUID ownerId, Long restaurantId, UpdateRestaurantRequest request) {
        Restaurant restaurant = getOwnedRestaurant(ownerId, restaurantId);
        restaurant.update(request.name(), request.phone(), request.address(),
                request.latitude(), request.longitude(), request.minOrderAmount(),
                request.deliveryFee(), request.estimatedDeliveryMinutes());
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse open(UUID ownerId, Long restaurantId) {
        Restaurant restaurant = getOwnedRestaurant(ownerId, restaurantId);
        restaurant.open();
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse close(UUID ownerId, Long restaurantId) {
        Restaurant restaurant = getOwnedRestaurant(ownerId, restaurantId);
        restaurant.close();
        return RestaurantResponse.from(restaurant);
    }

    public List<RestaurantResponse> getMyRestaurants(UUID ownerId) {
        User owner = findUser(ownerId);
        return restaurantRepository.findByOwner(owner).stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    public List<RestaurantResponse> getOpenRestaurants(RestaurantCategory category) {
        List<Restaurant> restaurants = (category != null)
                ? restaurantRepository.findByCategoryAndStatus(category, RestaurantStatus.OPEN)
                : restaurantRepository.findByStatus(RestaurantStatus.OPEN);
        return restaurants.stream().map(RestaurantResponse::from).toList();
    }

    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> BusinessException.notFound("가게를 찾을 수 없습니다."));
        List<MenuResponse> menus = menuRepository
                .findByRestaurantAndIsAvailableOrderBySortOrder(restaurant, true)
                .stream()
                .map(MenuResponse::from)
                .toList();
        return RestaurantDetailResponse.from(restaurant, menus);
    }

    @Transactional
    public MenuResponse createMenu(UUID ownerId, Long restaurantId, CreateMenuRequest request) {
        Restaurant restaurant = getOwnedRestaurant(ownerId, restaurantId);

        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .categoryName(request.categoryName())
                .sortOrder(request.sortOrder())
                .build();

        if (request.options() != null) {
            for (var optReq : request.options()) {
                MenuOption option = MenuOption.builder()
                        .menu(menu)
                        .name(optReq.name())
                        .isRequired(optReq.isRequired())
                        .maxSelectCount(optReq.maxSelectCount())
                        .build();

                if (optReq.items() != null) {
                    for (var itemReq : optReq.items()) {
                        MenuOptionItem item = MenuOptionItem.builder()
                                .menuOption(option)
                                .name(itemReq.name())
                                .extraPrice(itemReq.extraPrice())
                                .build();
                        option.getItems().add(item);
                    }
                }
                menu.getOptions().add(option);
            }
        }

        menuRepository.save(menu);
        return MenuResponse.from(menu);
    }

    @Transactional
    public MenuResponse updateMenu(UUID ownerId, Long menuId, UpdateMenuRequest request) {
        Menu menu = getOwnedMenu(ownerId, menuId);
        menu.update(request.name(), request.description(), request.price(),
                request.imageUrl(), request.categoryName(), request.sortOrder());
        return MenuResponse.from(menu);
    }

    @Transactional
    public void deleteMenu(UUID ownerId, Long menuId) {
        Menu menu = getOwnedMenu(ownerId, menuId);
        menuRepository.delete(menu);
    }

    @Transactional
    public MenuResponse updateMenuAvailability(UUID ownerId, Long menuId, boolean available) {
        Menu menu = getOwnedMenu(ownerId, menuId);
        menu.updateAvailability(available);
        return MenuResponse.from(menu);
    }

    private Restaurant getOwnedRestaurant(UUID ownerId, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> BusinessException.notFound("가게를 찾을 수 없습니다."));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("해당 가게에 대한 권한이 없습니다.");
        }
        return restaurant;
    }

    private Menu getOwnedMenu(UUID ownerId, Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> BusinessException.notFound("메뉴를 찾을 수 없습니다."));
        if (!menu.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw BusinessException.forbidden("해당 메뉴에 대한 권한이 없습니다.");
        }
        return menu;
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));
    }
}
