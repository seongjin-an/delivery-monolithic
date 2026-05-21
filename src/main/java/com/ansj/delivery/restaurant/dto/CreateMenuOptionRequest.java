package com.ansj.delivery.restaurant.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateMenuOptionRequest(
        @NotBlank(message = "옵션 이름은 필수입니다.")
        String name,

        boolean isRequired,
        int maxSelectCount,
        List<CreateMenuOptionItemRequest> items
) {}
