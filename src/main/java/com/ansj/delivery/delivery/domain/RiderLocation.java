package com.ansj.delivery.delivery.domain;

import com.ansj.delivery.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rider_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiderLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(updatable = false)
    private LocalDateTime recordedAt;

    @Builder
    public RiderLocation(User rider, BigDecimal latitude, BigDecimal longitude) {
        this.rider = rider;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = LocalDateTime.now();
    }
}
