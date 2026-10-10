package com.cropdeal.review.client;

import com.cropdeal.review.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {
	@GetMapping("/orders/internal/{id}")
	OrderResponse getOrder(@PathVariable("id") Long id, @RequestHeader("X-Internal-Key") String key);
}
