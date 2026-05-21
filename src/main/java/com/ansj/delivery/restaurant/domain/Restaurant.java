package com.ansj.delivery.restaurant.domain;

import com.ansj.delivery.common.entity.BaseEntity;
import com.ansj.delivery.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestaurantStatus status;

    private int minOrderAmount;

    private int deliveryFee;

    private int estimatedDeliveryMinutes;

    @Builder
    public Restaurant(User owner, String name, String phone, String address,
                      BigDecimal latitude, BigDecimal longitude,
                      RestaurantCategory category, int minOrderAmount,
                      int deliveryFee, int estimatedDeliveryMinutes) {
        this.owner = owner;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.status = RestaurantStatus.PREPARING;
        this.minOrderAmount = minOrderAmount;
        this.deliveryFee = deliveryFee;
        this.estimatedDeliveryMinutes = estimatedDeliveryMinutes;
    }

    public void open() {
        this.status = RestaurantStatus.OPEN;
    }

    public void close() {
        this.status = RestaurantStatus.CLOSED;
    }
}
