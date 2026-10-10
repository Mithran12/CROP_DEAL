package com.cropdeal.report.client;

import com.cropdeal.report.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @GetMapping("/payments")
    List<PaymentResponse> getAllPayments();
    
    @GetMapping("/payments/internal/order/{orderId}")
    PaymentResponse getPaymentByOrderInternal(@PathVariable Long orderId,@RequestHeader("X-Internal-Key") String key);

    @GetMapping("/payments/{id}")
    PaymentResponse getPaymentById(@PathVariable Long id);
}
