package com.ansj.delivery.order.repository;

import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.order.domain.OrderStatus;
import com.ansj.delivery.restaurant.domain.Restaurant;
import com.ansj.delivery.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findByRestaurantOrderByCreatedAtDesc(Restaurant restaurant);
    List<Order> findByRestaurantAndStatusOrderByCreatedAtDesc(Restaurant restaurant, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = 'READY_FOR_PICKUP' AND NOT EXISTS (SELECT d FROM Delivery d WHERE d.order = o)")
    List<Order> findAvailableForDelivery();
}
