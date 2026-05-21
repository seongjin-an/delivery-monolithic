package com.ansj.delivery.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String alias;

    @Column(nullable = false)
    private String address;

    private String detailAddress;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private boolean isDefault;

    @Builder
    public Address(User user, String alias, String address, String detailAddress,
                   BigDecimal latitude, BigDecimal longitude, boolean isDefault) {
        this.user = user;
        this.alias = alias;
        this.address = address;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
    }
}
