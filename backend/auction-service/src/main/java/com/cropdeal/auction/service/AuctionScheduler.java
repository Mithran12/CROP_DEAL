package com.cropdeal.auction.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuctionScheduler {

	private final AuctionService auctionService;

	public AuctionScheduler(AuctionService auctionService) {
		this.auctionService = auctionService;
	}

	@Scheduled(fixedRate = 3000)
	public void evaluateAuctionLifecycle() {
		try {
			auctionService.synchronizeAuctionStates();
		} catch (Exception ex) {
			System.err.println("Error in evaluateAuctionLifecycle: " + ex.getMessage());
		}
	}
}
