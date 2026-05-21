package com.ansj.delivery.restaurant.dto;

import com.ansj.delivery.restaurant.domain.MenuOptionItem;

public record MenuOptionItemResponse(Long id, String name, int extraPrice) {
    public static MenuOptionItemResponse from(MenuOptionItem item) {
        return new MenuOptionItemResponse(item.getId(), item.getName(), item.getExtraPrice());
    }
}
