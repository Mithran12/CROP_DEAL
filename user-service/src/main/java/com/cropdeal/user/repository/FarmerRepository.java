package com.cropdeal.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.user.entity.Farmer;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {

    Optional<Farmer> findByAuthUserId(Long authUserId);

    Optional<Farmer> findByEmail(String email);

    boolean existsByAuthUserId(Long authUserId);

    boolean existsByEmail(String email);
}