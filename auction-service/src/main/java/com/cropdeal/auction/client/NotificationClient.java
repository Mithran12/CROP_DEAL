package com.cropdeal.auction.client;

import com.cropdeal.auction.dto.CreateNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {

    @PostMapping("/notifications/internal")
    Object createNotification(
            @RequestHeader("X-Internal-Key") String internalKey,
            @RequestBody CreateNotificationRequest request
    );
}
