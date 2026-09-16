package com.cropdeal.subscription.controller;

import com.cropdeal.subscription.dto.CreateSubscriptionRequest;
import com.cropdeal.subscription.dto.SubscriptionResponse;
import com.cropdeal.subscription.dto.UpdateSubscriptionRequest;
import com.cropdeal.subscription.dto.UpdateSubscriptionStatusRequest;
import com.cropdeal.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Create a crop subscription for a dealer")
    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            Authentication authentication) {

        Long dealerId = authenticatedDealerId(authentication);
        if (!dealerId.equals(request.getDealerId())) {
            throw new IllegalArgumentException("Dealer ID does not match authenticated dealer");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.createSubscription(request));
    }

    @Operation(summary = "Get one subscription owned by the authenticated dealer")
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @PathVariable @Positive Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                subscriptionService.getSubscription(id, authenticatedDealerId(authentication)));
    }

    @Operation(summary = "Get all subscriptions for the authenticated dealer")
    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<List<SubscriptionResponse>> getDealerSubscriptions(
            @PathVariable @Positive Long dealerId,
            Authentication authentication) {

        requireOwner(dealerId, authentication);
        return ResponseEntity.ok(
                subscriptionService.getDealerSubscriptions(dealerId));
    }

    @Operation(summary = "Get active subscriptions for the authenticated dealer")
    @GetMapping("/dealer/{dealerId}/active")
    public ResponseEntity<List<SubscriptionResponse>> getActiveSubscriptions(
            @PathVariable @Positive Long dealerId,
            Authentication authentication) {

        requireOwner(dealerId, authentication);
        return ResponseEntity.ok(
                subscriptionService.getActiveSubscriptions(dealerId));
    }

    @Operation(summary = "Update a dealer subscription")
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateSubscriptionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                subscriptionService.updateSubscription(
                        id, request, authenticatedDealerId(authentication)));
    }

    @Operation(summary = "Change subscription status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<SubscriptionResponse> updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateSubscriptionStatusRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                subscriptionService.updateStatus(
                        id, request.getStatus(), authenticatedDealerId(authentication)));
    }

    @Operation(summary = "Cancel a dealer subscription")
    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable @Positive Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                subscriptionService.cancelSubscription(
                        id, authenticatedDealerId(authentication)));
    }

    private void requireOwner(Long dealerId, Authentication authentication) {
        if (!dealerId.equals(authenticatedDealerId(authentication))) {
            throw new IllegalArgumentException("Dealer ID does not match authenticated dealer");
        }
    }

    private Long authenticatedDealerId(Authentication authentication) {
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "JWT does not contain a numeric dealer identifier");
        }
    }
}
