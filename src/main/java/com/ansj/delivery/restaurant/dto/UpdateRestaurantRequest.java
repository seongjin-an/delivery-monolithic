package com.ansj.delivery.restaurant.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpdateRestaurantRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        String name,

        String phone,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        BigDecimal latitude,
        BigDecimal longitude,

        @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
        int minOrderAmount,

        @Min(value = 0, message = "배달료는 0원 이상이어야 합니다.")
        int deliveryFee,

        @Min(value = 1, message = "예상 배달 시간은 1분 이상이어야 합니다.")
        int estimatedDeliveryMinutes
) {}
