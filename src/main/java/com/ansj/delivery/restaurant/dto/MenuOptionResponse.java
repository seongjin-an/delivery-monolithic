package com.ansj.delivery.restaurant.dto;

import com.ansj.delivery.restaurant.domain.MenuOption;

import java.util.List;

public record MenuOptionResponse(
        Long id,
        String name,
        boolean isRequired,
        int maxSelectCount,
        List<MenuOptionItemResponse> items
) {
    public static MenuOptionResponse from(MenuOption option) {
        List<MenuOptionItemResponse> items = option.getItems().stream()
                .map(MenuOptionItemResponse::from)
                .toList();
        return new MenuOptionResponse(
                option.getId(), option.getName(), option.isRequired(), option.getMaxSelectCount(), items
        );
    }
}
