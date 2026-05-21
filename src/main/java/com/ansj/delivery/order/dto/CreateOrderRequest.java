package com.ansj.delivery.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "가게 ID는 필수입니다.")
        Long restaurantId,

        @NotBlank(message = "배달 주소는 필수입니다.")
        String deliveryAddress,

        BigDecimal deliveryLatitude,
        BigDecimal deliveryLongitude,
        String requestNote,

        @Valid
        @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")
        List<CreateOrderItemRequest> items
) {}
