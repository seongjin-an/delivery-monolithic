package com.ansj.delivery.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_COMPLETED,
    ACCEPTED,
    READY_FOR_PICKUP,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
