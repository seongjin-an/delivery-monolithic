package com.ansj.delivery.payment.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.payment.dto.PaymentRequest;
import com.ansj.delivery.payment.dto.PaymentResponse;
import com.ansj.delivery.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> pay(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PaymentRequest request) {
        return ApiResponse.ok("결제가 완료되었습니다.",
                paymentService.pay(UUID.fromString(userId), request));
    }

    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refund(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID paymentId) {
        return ApiResponse.ok("환불이 완료되었습니다.",
                paymentService.refund(UUID.fromString(userId), paymentId));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<PaymentResponse> getPaymentByOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID orderId) {
        return ApiResponse.ok(paymentService.getPaymentByOrder(UUID.fromString(userId), orderId));
    }
}
