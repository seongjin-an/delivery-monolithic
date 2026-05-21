package com.ansj.delivery.payment.repository;

import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrder(Order order);
}
