package com.ansj.delivery.restaurant.repository;

import com.ansj.delivery.restaurant.domain.Restaurant;
import com.ansj.delivery.restaurant.domain.RestaurantCategory;
import com.ansj.delivery.restaurant.domain.RestaurantStatus;
import com.ansj.delivery.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByCategoryAndStatus(RestaurantCategory category, RestaurantStatus status);
    List<Restaurant> findByOwner(User owner);
}
