package com.cropdeal.auction.controller;

import com.cropdeal.auction.dto.AuctionResponse;
import com.cropdeal.auction.dto.BidResponse;
import com.cropdeal.auction.dto.CreateAuctionRequest;
import com.cropdeal.auction.dto.PlaceBidRequest;
import com.cropdeal.auction.service.AuctionService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

	private final AuctionService auctionService;

	public AuctionController(AuctionService auctionService) {
		this.auctionService = auctionService;
	}

	@PostMapping
	public ResponseEntity<AuctionResponse> createAuction(@Valid @RequestBody CreateAuctionRequest request) {

		return ResponseEntity.ok(auctionService.createAuction(request));
	}

	@GetMapping("/open")
	public ResponseEntity<List<AuctionResponse>> getOpenAuctions() {
		return ResponseEntity.ok(auctionService.getOpenAuctions());
	}

	@GetMapping("/history")
	public ResponseEntity<List<AuctionResponse>> getAuctionHistory() {
		return ResponseEntity.ok(auctionService.getAuctionHistory());
	}

	@GetMapping("/closed")
	public ResponseEntity<List<AuctionResponse>> getClosedAuctions() {
		return ResponseEntity.ok(auctionService.getAuctionHistory());
	}

	@GetMapping("/farmer/{farmerId}")
	public ResponseEntity<List<AuctionResponse>> getFarmerAuctions(@PathVariable Long farmerId) {
		return ResponseEntity.ok(auctionService.getAuctionsByFarmer(farmerId));
	}

	@GetMapping("/dealer/{dealerId}")
	public ResponseEntity<List<AuctionResponse>> getDealerAuctions(@PathVariable Long dealerId) {
		return ResponseEntity.ok(auctionService.getAuctionsByDealer(dealerId));
	}

	@GetMapping
	public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
		return ResponseEntity.ok(auctionService.getAllAuctions());
	}

	@PatchMapping("/{id}/complete")
	public ResponseEntity<AuctionResponse> completeAuction(@PathVariable Long id) {
		return ResponseEntity.ok(auctionService.completeAuction(id));
	}

	@PatchMapping("/{id}/link-order")
	public ResponseEntity<AuctionResponse> linkOrder(@PathVariable Long id, @RequestParam Long orderId,
			@RequestParam(required = false) String orderNumber) {
		return ResponseEntity.ok(auctionService.linkOrder(id, orderId, orderNumber));
	}

	@GetMapping("/{auctionId}")
	public ResponseEntity<AuctionResponse> getAuction(@PathVariable Long auctionId) {
		return ResponseEntity.ok(auctionService.getAuctionById(auctionId));
	}

	@PostMapping("/{auctionId}/bids")
	public ResponseEntity<BidResponse> placeBid(@PathVariable Long auctionId,
			@Valid @RequestBody PlaceBidRequest request) {

		return ResponseEntity.ok(auctionService.placeBid(auctionId, request));
	}

	@GetMapping("/{auctionId}/bids")
	public ResponseEntity<List<BidResponse>> getAuctionBids(@PathVariable Long auctionId) {
		return ResponseEntity.ok(auctionService.getBidsByAuction(auctionId));
	}
}