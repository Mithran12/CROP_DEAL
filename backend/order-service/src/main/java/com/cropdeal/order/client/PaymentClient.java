package com.cropdeal.order.client;
import com.cropdeal.order.dto.*; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="PAYMENT-SERVICE")
public interface PaymentClient {
    @PostMapping("/payments")
    PaymentResponse create(@RequestBody SagaPaymentRequest request);

    @PostMapping("/payments/order/{orderId}/cancel")
    PaymentResponse cancelPayment(@PathVariable("orderId") Long orderId, @RequestHeader(value="X-Internal-Key", required=false) String internalKey);
}
