package com.cropdeal;

import com.cropdeal.subscription.dto.CreateSubscriptionRequest;
import com.cropdeal.subscription.dto.SubscriptionResponse;
import com.cropdeal.subscription.entity.Subscription;
import com.cropdeal.subscription.entity.SubscriptionStatus;
import com.cropdeal.subscription.repository.SubscriptionRepository;
import com.cropdeal.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceUnitTest {
    @Mock SubscriptionRepository repository;
    @InjectMocks SubscriptionService service;

    @Test
    void createSubscriptionCreatesActiveSubscription() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setDealerId(4L); request.setCropId(101L); request.setCategory("vegetables");
        when(repository.existsByDealerIdAndCropIdAndStatus(4L, 101L, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(repository.save(any(Subscription.class))).thenAnswer(inv -> {
            Subscription s = inv.getArgument(0); s.setId(1L); s.onCreate(); return s;
        });
        SubscriptionResponse response = service.createSubscription(request);
        assertEquals(1L, response.getId());
        assertEquals("VEGETABLES", response.getCategory());
        assertEquals("ACTIVE", response.getStatus());
        verify(repository).save(any(Subscription.class));
    }

    @Test
    void duplicateSubscriptionIsRejected() {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setDealerId(4L); request.setCropId(101L); request.setCategory("VEGETABLES");
        when(repository.existsByDealerIdAndCropIdAndStatus(4L, 101L, SubscriptionStatus.ACTIVE)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.createSubscription(request));
        verify(repository, never()).save(any());
    }

    @Test
    void getMissingSubscriptionThrowsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getSubscription(999L, 4L));
    }

    @Test
    void updateStatusChangesSubscriptionToCancelled() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setDealerId(4L);
        subscription.setCropId(101L);
        subscription.setCategory("VEGETABLES");
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(subscription));
        when(repository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionResponse response = service.updateStatus(1L, "CANCELLED", 4L);

        assertEquals("CANCELLED", response.getStatus());
        verify(repository).save(subscription);
    }

    @Test
    void foreignDealerCannotAccessSubscription() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setDealerId(4L);
        subscription.setCropId(101L);
        subscription.setCategory("VEGETABLES");
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThrows(IllegalArgumentException.class,
                () -> service.getSubscription(1L, 99L));
    }

    @Test
    void invalidStatusIsRejected() {
        Subscription subscription = new Subscription();
        subscription.setId(1L);
        subscription.setDealerId(4L);
        subscription.setCropId(101L);
        subscription.setCategory("VEGETABLES");
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateStatus(1L, "PAUSED", 4L));
        verify(repository, never()).save(any(Subscription.class));
    }
}