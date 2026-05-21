package com.ansj.delivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMenuOptionItemRequest(
        @NotBlank(message = "옵션 아이템 이름은 필수입니다.")
        String name,

        int extraPrice
) {}
