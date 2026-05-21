package com.ansj.delivery.order.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.order.domain.OrderStatus;
import com.ansj.delivery.order.dto.CancelOrderRequest;
import com.ansj.delivery.order.dto.CreateOrderRequest;
import com.ansj.delivery.order.dto.OrderResponse;
import com.ansj.delivery.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 고객 전용
    @PostMapping("/api/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok("주문이 접수되었습니다.",
                orderService.createOrder(UUID.fromString(userId), request));
    }

    @GetMapping("/api/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(orderService.getMyOrders(UUID.fromString(userId)));
    }

    @GetMapping("/api/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> getOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID orderId) {
        return ApiResponse.ok(orderService.getOrder(UUID.fromString(userId), orderId));
    }

    @PostMapping("/api/orders/{orderId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID orderId,
            @RequestBody CancelOrderRequest request) {
        return ApiResponse.ok("주문이 취소되었습니다.",
                orderService.cancelOrder(UUID.fromString(userId), orderId, request));
    }

    // 사장님 전용
    @GetMapping("/api/owner/restaurants/{restaurantId}/orders")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<List<OrderResponse>> getRestaurantOrders(
            @AuthenticationPrincipal String userId,
            @PathVariable Long restaurantId,
            @RequestParam(required = false) OrderStatus status) {
        return ApiResponse.ok(
                orderService.getRestaurantOrders(UUID.fromString(userId), restaurantId, status));
    }

    @PatchMapping("/api/owner/orders/{orderId}/accept")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OrderResponse> acceptOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID orderId) {
        return ApiResponse.ok("주문을 수락했습니다.",
                orderService.acceptOrder(UUID.fromString(userId), orderId));
    }

    @PatchMapping("/api/owner/orders/{orderId}/ready")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<OrderResponse> markReady(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID orderId) {
        return ApiResponse.ok("조리가 완료되었습니다.",
                orderService.markReady(UUID.fromString(userId), orderId));
    }
}
