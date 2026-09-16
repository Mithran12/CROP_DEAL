package com.cropdeal.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.order.entity.Negotiation;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    Optional<Negotiation> findByNegotiationNumber(String negotiationNumber);

    Optional<Negotiation> findByPurchaseRequestId(Long purchaseRequestId);
}