package com.ansj.delivery.payment.dto;

import com.ansj.delivery.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentMethod method
) {}
