package com.ansj.delivery.notification.dto;

import com.ansj.delivery.notification.domain.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getTitle(), n.getContent(),
                n.isRead(), n.getCreatedAt()
        );
    }
}
