package com.ansj.delivery.order.domain;

import com.ansj.delivery.common.entity.BaseEntity;
import com.ansj.delivery.common.exception.BusinessException;
import com.ansj.delivery.restaurant.domain.Restaurant;
import com.ansj.delivery.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private int deliveryFee;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(precision = 10, scale = 7)
    private BigDecimal deliveryLatitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal deliveryLongitude;

    private String requestNote;

    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(User customer, Restaurant restaurant, int totalAmount, int deliveryFee,
                 String deliveryAddress, BigDecimal deliveryLatitude, BigDecimal deliveryLongitude,
                 String requestNote) {
        this.customer = customer;
        this.restaurant = restaurant;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.deliveryAddress = deliveryAddress;
        this.deliveryLatitude = deliveryLatitude;
        this.deliveryLongitude = deliveryLongitude;
        this.requestNote = requestNote;
    }

    public void completePayment() {
        validateStatus(OrderStatus.PENDING_PAYMENT);
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }

    public void accept() {
        validateStatus(OrderStatus.PAYMENT_COMPLETED);
        this.status = OrderStatus.ACCEPTED;
    }

    public void readyForPickup() {
        validateStatus(OrderStatus.ACCEPTED);
        this.status = OrderStatus.READY_FOR_PICKUP;
    }

    public void pickup() {
        validateStatus(OrderStatus.READY_FOR_PICKUP);
        this.status = OrderStatus.PICKED_UP;
    }

    public void complete() {
        validateStatus(OrderStatus.PICKED_UP);
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel(String reason) {
        if (this.status == OrderStatus.PICKED_UP || this.status == OrderStatus.DELIVERED) {
            throw BusinessException.badRequest("픽업 이후 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
    }

    private void validateStatus(OrderStatus expected) {
        if (this.status != expected) {
            throw BusinessException.badRequest(
                    String.format("현재 상태(%s)에서는 해당 작업을 수행할 수 없습니다.", this.status));
        }
    }
}
