package com.ansj.delivery.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderItemRequest(
        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity,

        List<Long> selectedOptionItemIds
) {}
