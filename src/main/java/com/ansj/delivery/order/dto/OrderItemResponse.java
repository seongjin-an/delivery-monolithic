package com.ansj.delivery.order.dto;

import com.ansj.delivery.order.domain.OrderItem;

import java.util.List;

public record OrderItemResponse(
        Long id,
        String menuName,
        int quantity,
        int unitPrice,
        int totalPrice,
        List<OrderItemOptionResponse> options
) {
    public static OrderItemResponse from(OrderItem item) {
        List<OrderItemOptionResponse> options = item.getOptions().stream()
                .map(OrderItemOptionResponse::from)
                .toList();
        return new OrderItemResponse(
                item.getId(), item.getMenuName(), item.getQuantity(),
                item.getUnitPrice(), item.getTotalPrice(), options);
    }
}
