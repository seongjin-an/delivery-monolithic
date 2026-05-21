package com.ansj.delivery.restaurant.dto;

import com.ansj.delivery.restaurant.domain.Restaurant;
import com.ansj.delivery.restaurant.domain.RestaurantCategory;
import com.ansj.delivery.restaurant.domain.RestaurantStatus;

import java.math.BigDecimal;

public record RestaurantResponse(
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
        int estimatedDeliveryMinutes
) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(
                r.getId(), r.getName(), r.getPhone(), r.getAddress(),
                r.getLatitude(), r.getLongitude(), r.getCategory(), r.getStatus(),
                r.getMinOrderAmount(), r.getDeliveryFee(), r.getEstimatedDeliveryMinutes()
        );
    }
}
