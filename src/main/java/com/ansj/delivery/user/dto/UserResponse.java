package com.ansj.delivery.user.dto;

import com.ansj.delivery.user.domain.User;
import com.ansj.delivery.user.domain.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        String phone,
        UserRole role
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getPhone(), user.getRole());
    }
}
