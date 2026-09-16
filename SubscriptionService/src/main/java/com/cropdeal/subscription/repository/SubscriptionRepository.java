package com.cropdeal.subscription.repository;

import com.cropdeal.subscription.entity.Subscription;
import com.cropdeal.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    List<Subscription> findByDealerId(Long dealerId);

    List<Subscription> findByDealerIdAndStatus(
            Long dealerId,
            SubscriptionStatus status
    );

    boolean existsByDealerIdAndCropIdAndStatus(
            Long dealerId,
            Long cropId,
            SubscriptionStatus status
    );
}