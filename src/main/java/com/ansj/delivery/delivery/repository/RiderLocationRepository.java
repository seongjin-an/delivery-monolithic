package com.ansj.delivery.delivery.repository;

import com.ansj.delivery.delivery.domain.RiderLocation;
import com.ansj.delivery.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiderLocationRepository extends JpaRepository<RiderLocation, Long> {
    Optional<RiderLocation> findTopByRiderOrderByRecordedAtDesc(User rider);
}
