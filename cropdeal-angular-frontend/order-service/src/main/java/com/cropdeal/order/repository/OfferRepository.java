package com.cropdeal.order.repository;

import com.cropdeal.order.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OfferRepository extends JpaRepository<Offer, Long> {
	List<Offer> findByNegotiationIdOrderByCreatedAtAsc(Long negotiationId);
}
