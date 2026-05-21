package com.ansj.delivery.payment.domain;

import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.order.domain.Order;
import com.ansj.delivery.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String pgTransactionId;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Payment(Order order, User customer, int amount, PaymentMethod method) {
        this.order = order;
        this.customer = customer;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void complete(String pgTransactionId) {
        validateStatus(PaymentStatus.PENDING);
        this.status = PaymentStatus.COMPLETED;
        this.pgTransactionId = pgTransactionId;
        this.paidAt = LocalDateTime.now();
    }

    public void fail() {
        validateStatus(PaymentStatus.PENDING);
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        validateStatus(PaymentStatus.COMPLETED);
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
    }

    private void validateStatus(PaymentStatus expected) {
        if (this.status != expected) {
            throw BusinessException.badRequest(
                    String.format("현재 결제 상태(%s)에서는 해당 작업을 수행할 수 없습니다.", this.status));
        }
    }
}
