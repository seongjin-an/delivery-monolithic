package com.ansj.delivery.order.dto;

import com.ansj.delivery.order.domain.OrderItemOption;

public record OrderItemOptionResponse(String optionName, String optionItemName, int extraPrice) {
    public static OrderItemOptionResponse from(OrderItemOption option) {
        return new OrderItemOptionResponse(
                option.getOptionName(), option.getOptionItemName(), option.getExtraPrice());
    }
}
