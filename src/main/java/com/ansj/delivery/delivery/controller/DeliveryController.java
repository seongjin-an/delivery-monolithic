package com.ansj.delivery.delivery.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.delivery.domain.DeliveryStatus;
import com.ansj.delivery.delivery.dto.AvailableDeliveryResponse;
import com.ansj.delivery.delivery.dto.DeliveryResponse;
import com.ansj.delivery.delivery.dto.RiderLocationRequest;
import com.ansj.delivery.delivery.service.DeliveryService;
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
public class DeliveryController {

    private final DeliveryService deliveryService;

    // 라이더 전용
    @GetMapping("/api/rider/deliveries/available")
    @PreAuthorize("hasRole('RIDER')")
    public ApiResponse<List<AvailableDeliveryResponse>> getAvailableDeliveries() {
        return ApiResponse.ok(deliveryService.getAvailableDeliveries());
    }

    @GetMapping("/api/rider/deliveries")
    @PreAuthorize("hasRole('RIDER')")
    public ApiResponse<List<DeliveryResponse>> getMyDeliveries(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) DeliveryStatus status) {
        return ApiResponse.ok(deliveryService.getMyDeliveries(UUID.fromString(userId), status));
    }

    @PostMapping("/api/rider/deliveries/{orderId}/accept")
    @PreAuthorize("hasRole('RIDER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeliveryResponse> acceptDelivery(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID orderId) {
        return ApiResponse.ok("배달을 수락했습니다.",
                deliveryService.acceptDelivery(UUID.fromString(userId), orderId));
    }

    @PatchMapping("/api/rider/deliveries/{deliveryId}/pickup")
    @PreAuthorize("hasRole('RIDER')")
    public ApiResponse<DeliveryResponse> pickup(
            @AuthenticationPrincipal String userId,
            @PathVariable Long deliveryId) {
        return ApiResponse.ok("픽업 완료 처리되었습니다.",
                deliveryService.pickup(UUID.fromString(userId), deliveryId));
    }

    @PatchMapping("/api/rider/deliveries/{deliveryId}/complete")
    @PreAuthorize("hasRole('RIDER')")
    public ApiResponse<DeliveryResponse> complete(
            @AuthenticationPrincipal String userId,
            @PathVariable Long deliveryId) {
        return ApiResponse.ok("배달이 완료되었습니다.",
                deliveryService.complete(UUID.fromString(userId), deliveryId));
    }

    @PostMapping("/api/rider/locations")
    @PreAuthorize("hasRole('RIDER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocation(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody RiderLocationRequest request) {
        deliveryService.updateLocation(UUID.fromString(userId), request);
    }

    // 고객용 배달 현황 조회
    @GetMapping("/api/orders/{orderId}/delivery")
    public ApiResponse<DeliveryResponse> getDeliveryByOrder(@PathVariable UUID orderId) {
        return ApiResponse.ok(deliveryService.getDeliveryByOrder(orderId));
    }
}
