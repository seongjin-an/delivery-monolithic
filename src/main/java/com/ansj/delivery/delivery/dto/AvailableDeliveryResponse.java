package com.ansj.delivery.delivery.dto;

import com.ansj.delivery.order.domain.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailableDeliveryResponse(
        UUID orderId,
        String restaurantName,
        String restaurantAddress,
        String deliveryAddress,
        BigDecimal deliveryLatitude,
        BigDecimal deliveryLongitude,
        int deliveryFee
) {
    public static AvailableDeliveryResponse from(Order order) {
        return new AvailableDeliveryResponse(
                order.getId(),
                order.getRestaurant().getName(),
                order.getRestaurant().getAddress(),
                order.getDeliveryAddress(),
                order.getDeliveryLatitude(),
                order.getDeliveryLongitude(),
                order.getDeliveryFee()
        );
    }
}
