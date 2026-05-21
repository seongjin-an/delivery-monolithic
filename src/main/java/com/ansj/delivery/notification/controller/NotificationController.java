package com.ansj.delivery.notification.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.notification.dto.NotificationResponse;
import com.ansj.delivery.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) Boolean unreadOnly) {
        return ApiResponse.ok(notificationService.getMyNotifications(UUID.fromString(userId), unreadOnly));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id) {
        return ApiResponse.ok(notificationService.markAsRead(UUID.fromString(userId), id));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal String userId) {
        notificationService.markAllAsRead(UUID.fromString(userId));
        return ApiResponse.ok("모든 알림을 읽음 처리했습니다.", null);
    }
}
