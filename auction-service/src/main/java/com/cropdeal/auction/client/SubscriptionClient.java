package com.cropdeal.auction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "SUBSCRIPTION-SERVICE")
public interface SubscriptionClient {

	@GetMapping("/subscriptions/internal/matching-dealers")
	List<Long> getMatchingDealers(@RequestParam(value = "cropName", required = false) String cropName,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "location", required = false) String location,
			@RequestParam(value = "quantity", required = false) Double quantity,
			@RequestParam(value = "price", required = false) Double price);
}
