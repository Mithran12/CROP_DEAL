package com.cropdeal.order.repository;
import com.cropdeal.order.entity.NegotiationOffer; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface NegotiationOfferRepository extends JpaRepository<NegotiationOffer,Long> { List<NegotiationOffer> findByOrderIdOrderByCreatedAtAsc(Long orderId); }
