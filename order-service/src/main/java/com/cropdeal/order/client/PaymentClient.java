package com.cropdeal.order.client;
import com.cropdeal.order.dto.OrderResponse; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="PAYMENT-SERVICE") public interface PaymentClient { @PostMapping("/payments/internal/{orderId}/escrow/release") void releaseEscrow(@PathVariable Long orderId,@RequestHeader("X-Internal-Key") String key); }
