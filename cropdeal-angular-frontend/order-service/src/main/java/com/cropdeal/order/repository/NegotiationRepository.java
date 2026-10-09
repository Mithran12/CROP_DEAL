package com.cropdeal.order.repository;

import com.cropdeal.order.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {
	List<Negotiation> findByCropIdAndDealerId(Long cropId, Long dealerId);

	List<Negotiation> findByDealerIdOrderByUpdatedAtDesc(Long dealerId);

	List<Negotiation> findByFarmerIdOrderByUpdatedAtDesc(Long farmerId);

	Optional<Negotiation> findByOrderId(Long orderId);
}
