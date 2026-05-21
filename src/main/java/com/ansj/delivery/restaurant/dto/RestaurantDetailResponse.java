package com.ansj.delivery.restaurant.dto;

import com.ansj.delivery.restaurant.domain.Restaurant;
import com.ansj.delivery.restaurant.domain.RestaurantCategory;
import com.ansj.delivery.restaurant.domain.RestaurantStatus;

import java.math.BigDecimal;
import java.util.List;

public record RestaurantDetailResponse(
        Long id,
        String name,
        String phone,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        RestaurantCategory category,
        RestaurantStatus status,
        int minOrderAmount,
        int deliveryFee,
        int estimatedDeliveryMinutes,
        List<MenuResponse> menus
) {
    public static RestaurantDetailResponse from(Restaurant r, List<MenuResponse> menus) {
        return new RestaurantDetailResponse(
                r.getId(), r.getName(), r.getPhone(), r.getAddress(),
                r.getLatitude(), r.getLongitude(), r.getCategory(), r.getStatus(),
                r.getMinOrderAmount(), r.getDeliveryFee(), r.getEstimatedDeliveryMinutes(),
                menus
        );
    }
}
