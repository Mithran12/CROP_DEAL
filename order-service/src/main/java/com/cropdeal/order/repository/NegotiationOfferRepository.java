package com.cropdeal.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cropdeal.order.entity.NegotiationOffer;

public interface NegotiationOfferRepository extends JpaRepository<NegotiationOffer, Long> {

    List<NegotiationOffer> findByNegotiationIdOrderByCreatedAtAsc(Long negotiationId);
}