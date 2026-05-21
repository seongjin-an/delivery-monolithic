package com.ansj.delivery.notification.service;

import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.notification.domain.Notification;
import com.ansj.delivery.notification.dto.NotificationResponse;
import com.ansj.delivery.notification.repository.NotificationRepository;
import com.ansj.delivery.user.domain.User;
import com.ansj.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 다른 서비스에서 호출하는 알림 발송 메서드
    @Transactional
    public void send(User user, String type, String title, String content) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getMyNotifications(UUID userId, Boolean unreadOnly) {
        User user = findUser(userId);
        List<Notification> notifications = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                : notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public NotificationResponse markAsRead(UUID userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> BusinessException.notFound("알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("해당 알림에 대한 권한이 없습니다.");
        }

        notification.read();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        User user = findUser(userId);
        notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .forEach(Notification::read);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));
    }
}
