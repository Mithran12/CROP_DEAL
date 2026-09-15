package com.cropdeal.payment.repository;

import com.cropdeal.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentEventRepository
        extends JpaRepository<PaymentEvent, Long> {

    List<PaymentEvent> findByPaymentIdOrderByCreatedAtAsc(Long paymentId);
}