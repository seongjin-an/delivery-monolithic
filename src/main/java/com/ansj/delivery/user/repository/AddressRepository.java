package com.ansj.delivery.user.repository;

import com.ansj.delivery.user.domain.Address;
import com.ansj.delivery.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}
