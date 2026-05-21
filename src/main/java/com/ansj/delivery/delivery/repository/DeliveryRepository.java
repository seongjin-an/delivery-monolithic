package com.ansj.delivery.delivery.repository;

import com.ansj.delivery.delivery.domain.Delivery;
import com.ansj.delivery.delivery.domain.DeliveryStatus;
import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrder(Order order);
    List<Delivery> findByRiderOrderByAssignedAtDesc(User rider);
    List<Delivery> findByRiderAndStatusOrderByAssignedAtDesc(User rider, DeliveryStatus status);
}
