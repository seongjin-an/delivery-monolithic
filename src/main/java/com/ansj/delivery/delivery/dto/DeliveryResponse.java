package com.ansj.delivery.delivery.dto;

import com.ansj.delivery.delivery.domain.Delivery;
import com.ansj.delivery.delivery.domain.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponse(
        Long id,
        UUID orderId,
        String deliveryAddress,
        UUID riderId,
        String riderName,
        DeliveryStatus status,
        LocalDateTime assignedAt,
        LocalDateTime pickedUpAt,
        LocalDateTime deliveredAt
) {
    public static DeliveryResponse from(Delivery d) {
        return new DeliveryResponse(
                d.getId(),
                d.getOrder().getId(),
                d.getOrder().getDeliveryAddress(),
                d.getRider().getId(),
                d.getRider().getName(),
                d.getStatus(),
                d.getAssignedAt(),
                d.getPickedUpAt(),
                d.getDeliveredAt()
        );
    }
}
