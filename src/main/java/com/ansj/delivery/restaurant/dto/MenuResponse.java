package com.ansj.delivery.restaurant.dto;

import com.ansj.delivery.restaurant.domain.Menu;

import java.util.List;

public record MenuResponse(
        Long id,
        String name,
        String description,
        int price,
        String imageUrl,
        boolean isAvailable,
        String categoryName,
        int sortOrder,
        List<MenuOptionResponse> options
) {
    public static MenuResponse from(Menu menu) {
        List<MenuOptionResponse> options = menu.getOptions().stream()
                .map(MenuOptionResponse::from)
                .toList();
        return new MenuResponse(
                menu.getId(), menu.getName(), menu.getDescription(), menu.getPrice(),
                menu.getImageUrl(), menu.isAvailable(), menu.getCategoryName(),
                menu.getSortOrder(), options
        );
    }
}
