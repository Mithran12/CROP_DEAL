package com.cropdeal.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.cropdeal.order.dto.request.CounterOfferRequestDto;
import com.cropdeal.order.dto.request.CreateOfferRequestDto;
import com.cropdeal.order.dto.response.NegotiationResponseDto;
import com.cropdeal.order.dto.response.OfferResponseDto;
import com.cropdeal.order.service.NegotiationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/negotiations")
@Tag(name = "Negotiation Management", description = "APIs for creating negotiations and managing offers")
public class NegotiationController {

	private final NegotiationService negotiationService;

	public NegotiationController(NegotiationService negotiationService) {

		this.negotiationService = negotiationService;
	}

	// CREATE NEGOTIATION

	@Operation(summary = "Create a negotiation", description = "Creates a negotiation for an existing purchase request.")
	@PostMapping("/{purchaseRequestId}")
	public ResponseEntity<NegotiationResponseDto> createNegotiation(@PathVariable Long purchaseRequestId,
			@AuthenticationPrincipal Jwt jwt) {

		Long authenticatedUserId = ((Number) jwt.getClaim("userId")).longValue();

		NegotiationResponseDto response = negotiationService.createNegotiation(purchaseRequestId, authenticatedUserId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// GET NEGOTIATION

	@Operation(summary = "Get negotiation by ID", description = "Returns a negotiation when the authenticated user is the farmer or dealer associated with it.")
	@GetMapping("/{id}")
	public ResponseEntity<NegotiationResponseDto> getNegotiationById(@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt) {

		Long authenticatedUserId = ((Number) jwt.getClaim("userId")).longValue();

		NegotiationResponseDto response = negotiationService.getNegotiationById(id, authenticatedUserId);

		return ResponseEntity.ok(response);
	}

	// GET OFFERS

	@Operation(summary = "Get negotiation offers", description = "Returns all offers associated with a negotiation in chronological order.")
	@GetMapping("/{id}/offers")
	public ResponseEntity<List<OfferResponseDto>> getOffers(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

		Long authenticatedUserId = ((Number) jwt.getClaim("userId")).longValue();

		List<OfferResponseDto> response = negotiationService.getOffers(id, authenticatedUserId);

		return ResponseEntity.ok(response);
	}

	// CREATE OFFER

	@Operation(summary = "Create an offer", description = "Creates a new offer in an active negotiation for the authenticated user.")
	@PostMapping("/{id}/offer")
	public ResponseEntity<OfferResponseDto> createOffer(@PathVariable Long id,
			@Valid @RequestBody CreateOfferRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {

		Long userId = ((Number) jwt.getClaim("userId")).longValue();

		String role = jwt.getClaimAsString("role");

		OfferResponseDto response = negotiationService.createOffer(id, requestDto, userId, role);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// CREATE COUNTER OFFER

	@Operation(summary = "Create a counter offer", description = "Creates a counter offer for an existing negotiation.")
	@PostMapping("/{id}/counter-offer")
	public ResponseEntity<OfferResponseDto> createCounterOffer(@PathVariable Long id,
			@Valid @RequestBody CounterOfferRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {

		Long userId = ((Number) jwt.getClaim("userId")).longValue();

		String role = jwt.getClaimAsString("role");

		OfferResponseDto response = negotiationService.createCounterOffer(id, requestDto, userId, role);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// ACCEPT OFFER

	@Operation(summary = "Accept an offer", description = "Accepts the selected offer and finalizes the negotiation.")
	@PatchMapping("/{id}/accept/{offerId}")
	public ResponseEntity<NegotiationResponseDto> acceptOffer(@PathVariable Long id, @PathVariable Long offerId,
			@AuthenticationPrincipal Jwt jwt) {

		Long authenticatedUserId = ((Number) jwt.getClaim("userId")).longValue();

		NegotiationResponseDto response = negotiationService.acceptOffer(id, offerId, authenticatedUserId);

		return ResponseEntity.ok(response);
	}

	// REJECT OFFER

	@Operation(summary = "Reject an offer", description = "Rejects the selected offer in the negotiation.")
	@PatchMapping("/{id}/reject/{offerId}")
	public ResponseEntity<NegotiationResponseDto> rejectOffer(@PathVariable Long id, @PathVariable Long offerId,
			@AuthenticationPrincipal Jwt jwt) {

		Long authenticatedUserId = ((Number) jwt.getClaim("userId")).longValue();

		NegotiationResponseDto response = negotiationService.rejectOffer(id, offerId, authenticatedUserId);

		return ResponseEntity.ok(response);
	}
}