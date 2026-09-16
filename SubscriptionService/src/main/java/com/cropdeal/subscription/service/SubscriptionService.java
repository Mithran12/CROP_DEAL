package com.cropdeal.subscription.service;

import com.cropdeal.subscription.dto.CreateSubscriptionRequest;
import com.cropdeal.subscription.dto.SubscriptionResponse;
import com.cropdeal.subscription.dto.UpdateSubscriptionRequest;
import com.cropdeal.subscription.entity.Subscription;
import com.cropdeal.subscription.entity.SubscriptionStatus;
import com.cropdeal.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Creating subscription for dealerId={}, cropId={}", request.getDealerId(), request.getCropId());
        if (subscriptionRepository.existsByDealerIdAndCropIdAndStatus(
                request.getDealerId(), request.getCropId(), SubscriptionStatus.ACTIVE)) {
            log.warn("Duplicate active subscription for dealerId={}, cropId={}", request.getDealerId(), request.getCropId());
            throw new IllegalArgumentException("Dealer is already subscribed to this crop");
        }
        Subscription subscription = new Subscription();
        subscription.setDealerId(request.getDealerId());
        subscription.setCropId(request.getCropId());
        subscription.setCategory(request.getCategory().trim().toUpperCase());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Subscription created successfully with id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscription(Long id, Long dealerId) {
        log.info("Fetching subscription id={}", id);
        return toResponse(findOwnedById(id, dealerId));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getDealerSubscriptions(Long dealerId) {
        log.info("Fetching subscriptions for dealerId={}", dealerId);
        return subscriptionRepository.findByDealerId(dealerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getActiveSubscriptions(Long dealerId) {
        log.info("Fetching active subscriptions for dealerId={}", dealerId);
        return subscriptionRepository.findByDealerIdAndStatus(dealerId, SubscriptionStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public SubscriptionResponse updateSubscription(Long id, UpdateSubscriptionRequest request, Long dealerId) {
        Subscription subscription = findOwnedById(id, dealerId);
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled subscription cannot be updated");
        }
        boolean duplicate = subscriptionRepository.existsByDealerIdAndCropIdAndStatus(
                subscription.getDealerId(), request.getCropId(), SubscriptionStatus.ACTIVE);
        if (duplicate && !request.getCropId().equals(subscription.getCropId())) {
            throw new IllegalArgumentException("Dealer is already subscribed to this crop");
        }
        subscription.setCropId(request.getCropId());
        subscription.setCategory(request.getCategory().trim().toUpperCase());
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponse updateStatus(Long id, String status, Long dealerId) {
        Subscription subscription = findOwnedById(id, dealerId);
        final SubscriptionStatus newStatus;
        try {
            newStatus = SubscriptionStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Status must be ACTIVE or CANCELLED");
        }
        subscription.setStatus(newStatus);
        log.info("Subscription id={} status changed to {}", id, newStatus);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long id, Long dealerId) {
        return updateStatus(id, "CANCELLED", dealerId);
    }

    private Subscription findById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
    }

    private Subscription findOwnedById(Long id, Long dealerId) {
        Subscription subscription = findById(id);
        if (!dealerId.equals(subscription.getDealerId())) {
            throw new IllegalArgumentException("Subscription does not belong to authenticated dealer");
        }
        return subscription;
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setDealerId(subscription.getDealerId());
        response.setCropId(subscription.getCropId());
        response.setCategory(subscription.getCategory());
        response.setStatus(subscription.getStatus().name());
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());
        return response;
    }
}
