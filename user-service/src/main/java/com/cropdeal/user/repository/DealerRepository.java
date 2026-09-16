package com.cropdeal.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.user.entity.Dealer;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    Optional<Dealer> findByAuthUserId(Long authUserId);

    Optional<Dealer> findByEmail(String email);

    boolean existsByAuthUserId(Long authUserId);

    boolean existsByEmail(String email);
}