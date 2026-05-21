package com.ansj.delivery.restaurant.dto;

import com.ansj.delivery.restaurant.domain.RestaurantCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRestaurantRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        String name,

        String phone,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        BigDecimal latitude,
        BigDecimal longitude,

        @NotNull(message = "카테고리는 필수입니다.")
        RestaurantCategory category,

        @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
        int minOrderAmount,

        @Min(value = 0, message = "배달료는 0원 이상이어야 합니다.")
        int deliveryFee,

        @Min(value = 1, message = "예상 배달 시간은 1분 이상이어야 합니다.")
        int estimatedDeliveryMinutes
) {}
