package com.ansj.delivery.delivery.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RiderLocationRequest(
        @NotNull(message = "위도는 필수입니다.")
        BigDecimal latitude,

        @NotNull(message = "경도는 필수입니다.")
        BigDecimal longitude
) {}
