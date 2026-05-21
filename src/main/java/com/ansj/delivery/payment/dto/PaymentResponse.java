package com.ansj.delivery.payment.dto;

import com.ansj.delivery.payment.domain.Payment;
import com.ansj.delivery.payment.domain.PaymentMethod;
import com.ansj.delivery.payment.domain.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        int amount,
        PaymentMethod method,
        PaymentStatus status,
        String pgTransactionId,
        LocalDateTime paidAt,
        LocalDateTime refundedAt,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getAmount(),
                p.getMethod(),
                p.getStatus(),
                p.getPgTransactionId(),
                p.getPaidAt(),
                p.getRefundedAt(),
                p.getCreatedAt()
        );
    }
}
