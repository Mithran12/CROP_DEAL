package com.cropdeal.order.controller;

import com.cropdeal.order.dto.*;
import com.cropdeal.order.service.NegotiationService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/negotiations")
public class NegotiationController {
	private final NegotiationService service;

	public NegotiationController(NegotiationService s) {
		service = s;
	}

	@PostMapping
	public ResponseEntity<NegotiationResponse> start(@Valid @RequestBody NegotiationRequest r) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.start(r));
	}

	@PostMapping("/{id}/offers")
	public NegotiationResponse offer(@PathVariable Long id, @Valid @RequestBody OfferRequest r) {
		return service.dealerOffer(id, r);
	}

	@PostMapping("/{id}/counter")
	public NegotiationResponse counter(@PathVariable Long id, @Valid @RequestBody OfferRequest r) {
		return service.counter(id, r);
	}

	@PostMapping("/{id}/accept")
	public NegotiationResponse accept(@PathVariable Long id) {
		return service.accept(id);
	}

	@PostMapping("/{id}/reject")
	public NegotiationResponse reject(@PathVariable Long id) {
		return service.reject(id);
	}

	@GetMapping("/{id}")
	public NegotiationResponse get(@PathVariable Long id) {
		return service.get(id);
	}

	@GetMapping("/dealer/{dealerId}")
	public java.util.List<NegotiationResponse> getByDealer(@PathVariable Long dealerId) {
		return service.getByDealer(dealerId);
	}

	@GetMapping("/farmer/{farmerId}")
	public java.util.List<NegotiationResponse> getByFarmer(@PathVariable Long farmerId) {
		return service.getByFarmer(farmerId);
	}

	@GetMapping
	public java.util.List<NegotiationResponse> getAll() {
		return service.getAllNegotiations();
	}
}
