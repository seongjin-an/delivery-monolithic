package com.ansj.delivery.order.service;

import com.ansj.delivery.restaurant.domain.Menu;
import com.ansj.delivery.restaurant.domain.MenuOptionItem;
import java.util.List;

public record PreparedItem(Menu menu, int quantity, int unitPrice, List<MenuOptionItem> optionItems) {

}
