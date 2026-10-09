package com.cropdeal.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment,Long>{
	
	Optional<Payment> findByOrderId(Long orderId);
	
	boolean existByOrderId(Long orderId);
}
