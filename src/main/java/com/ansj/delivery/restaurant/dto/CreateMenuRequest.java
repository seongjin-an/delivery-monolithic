package com.ansj.delivery.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateMenuRequest(
        @NotBlank(message = "메뉴 이름은 필수입니다.")
        String name,

        String description,

        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        int price,

        String imageUrl,
        String categoryName,
        int sortOrder,
        List<CreateMenuOptionRequest> options
) {}
