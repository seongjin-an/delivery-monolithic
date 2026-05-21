package com.ansj.delivery.restaurant.repository;

import com.ansj.delivery.restaurant.domain.Menu;
import com.ansj.delivery.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByRestaurantAndIsAvailableOrderBySortOrder(Restaurant restaurant, boolean isAvailable);
}
