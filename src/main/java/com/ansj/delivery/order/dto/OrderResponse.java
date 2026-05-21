package com.ansj.delivery.order.dto;

import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.order.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        Long restaurantId,
        String restaurantName,
        OrderStatus status,
        int totalAmount,
        int deliveryFee,
        String deliveryAddress,
        String requestNote,
        String cancelReason,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDeliveryFee(),
                order.getDeliveryAddress(),
                order.getRequestNote(),
                order.getCancelReason(),
                items,
                order.getCreatedAt()
        );
    }
}
