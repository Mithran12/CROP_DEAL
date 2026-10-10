package com.cropdeal.pricing.controller;

import com.cropdeal.pricing.dto.MarketRateRequest;
import com.cropdeal.pricing.dto.MarketRateResponse;
import com.cropdeal.pricing.dto.PricingRequest;
import com.cropdeal.pricing.dto.PricingResponse;
import com.cropdeal.pricing.entity.MarketRate;
import com.cropdeal.pricing.repository.MarketRateRepository;
import com.cropdeal.pricing.service.PricingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pricing")
public class PricingController {

	private final PricingService pricingService;
	private final MarketRateRepository marketRateRepository;

	public PricingController(PricingService pricingService, MarketRateRepository marketRateRepository) {
		this.pricingService = pricingService;
		this.marketRateRepository = marketRateRepository;
	}

	@PostMapping("/calculate")
	public ResponseEntity<PricingResponse> calculatePrice(@Valid @RequestBody PricingRequest request) {

		return ResponseEntity.ok(pricingService.calculateFairPrice(request));
	}

	@GetMapping("/market-rates")
	public ResponseEntity<List<MarketRateResponse>> getAllMarketRates(@RequestParam(required = false) String category,
			@RequestParam(required = false) String search) {

		List<MarketRateResponse> rates = marketRateRepository.findAll().stream().filter(r -> {
			if (category != null && !category.trim().isEmpty()) {
				if (r.getCategory() == null || !r.getCategory().toUpperCase().contains(category.trim().toUpperCase())) {
					return false;
				}
			}
			if (search != null && !search.trim().isEmpty()) {
				String q = search.trim().toLowerCase();
				boolean matchName = r.getCropName() != null && r.getCropName().toLowerCase().contains(q);
				boolean matchDist = r.getDistrict() != null && r.getDistrict().toLowerCase().contains(q);
				boolean matchState = r.getState() != null && r.getState().toLowerCase().contains(q);
				boolean matchLoc = r.getLocation() != null && r.getLocation().toLowerCase().contains(q);
				if (!matchName && !matchDist && !matchState && !matchLoc) {
					return false;
				}
			}
			return true;
		}).map(this::mapToResponse).toList();

		return ResponseEntity.ok(rates);
	}

	@GetMapping("/market-rates/{cropName}")
	public ResponseEntity<List<MarketRateResponse>> getMarketRatesByCrop(@PathVariable String cropName) {
		return ResponseEntity.ok(marketRateRepository.findAll().stream()
				.filter(r -> r.getCropName() != null && r.getCropName().equalsIgnoreCase(cropName.trim()))
				.map(this::mapToResponse).toList());
	}

	@PostMapping("/market-rates")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<MarketRateResponse> addMarketRate(@Valid @RequestBody MarketRateRequest request) {

		MarketRate rate = new MarketRate();
		rate.setCropName(request.getCropName().trim());
		rate.setCategory(request.getCategory().trim());
		rate.setState(request.getState().trim());
		rate.setDistrict(request.getDistrict().trim());
		rate.setLocation(request.getDistrict().trim() + ", " + request.getState().trim());
		Double mPrice = request.getMarketPrice() != null ? request.getMarketPrice() : request.getPricePerQuintal();
		rate.setMarketPrice(mPrice);
		rate.setMaxPrice(request.getMaxPrice());
		rate.setMinPrice(request.getMinPrice() != null ? request.getMinPrice() : mPrice);
		rate.setModalPrice(request.getModalPrice() != null ? request.getModalPrice() : mPrice);
		rate.setPricePerQuintal(mPrice);
		rate.setImageUrl(request.getImageUrl());
		rate.setUpdatedAt(LocalDateTime.now());

		MarketRate saved = marketRateRepository.save(rate);
		return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));
	}

	@PutMapping("/market-rates/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<MarketRateResponse> updateMarketRate(@PathVariable Long id,
			@Valid @RequestBody MarketRateRequest request) {

		MarketRate rate = marketRateRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Market rate not found with id: " + id));

		rate.setCropName(request.getCropName().trim());
		rate.setCategory(request.getCategory().trim());
		rate.setState(request.getState().trim());
		rate.setDistrict(request.getDistrict().trim());
		rate.setLocation(request.getDistrict().trim() + ", " + request.getState().trim());
		Double mPrice = request.getMarketPrice() != null ? request.getMarketPrice() : request.getPricePerQuintal();
		rate.setMarketPrice(mPrice);
		rate.setMaxPrice(request.getMaxPrice());
		rate.setMinPrice(request.getMinPrice() != null ? request.getMinPrice() : mPrice);
		rate.setModalPrice(request.getModalPrice() != null ? request.getModalPrice() : mPrice);
		rate.setPricePerQuintal(mPrice);
		if (request.getImageUrl() != null) {
			rate.setImageUrl(request.getImageUrl());
		}
		rate.setUpdatedAt(LocalDateTime.now());

		MarketRate updated = marketRateRepository.save(rate);
		return ResponseEntity.ok(mapToResponse(updated));
	}

	@DeleteMapping("/market-rates/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteMarketRate(@PathVariable Long id) {
		if (!marketRateRepository.existsById(id)) {
			throw new IllegalArgumentException("Market rate not found with id: " + id);
		}
		marketRateRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	private MarketRateResponse mapToResponse(MarketRate r) {
		MarketRateResponse resp = new MarketRateResponse();
		resp.setId(r.getId());
		resp.setCropName(r.getCropName());
		resp.setCategory(r.getCategory());
		resp.setState(r.getState());
		resp.setDistrict(r.getDistrict());
		resp.setLocation(r.getLocation());
		Double mPrice = r.getMarketPrice() != null ? r.getMarketPrice() : r.getPricePerQuintal();
		resp.setMarketPrice(mPrice);
		resp.setMaxPrice(r.getMaxPrice());
		resp.setImageUrl(r.getImageUrl());
		resp.setMinPrice(r.getMinPrice() != null ? r.getMinPrice() : mPrice);
		resp.setModalPrice(r.getModalPrice() != null ? r.getModalPrice() : mPrice);
		resp.setPricePerQuintal(mPrice);
		resp.setUpdatedAt(r.getUpdatedAt());
		return resp;
	}
}