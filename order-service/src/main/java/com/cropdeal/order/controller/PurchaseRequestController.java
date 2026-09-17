package com.cropdeal.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.cropdeal.order.dto.request.CreatePurchaseRequestDto;
import com.cropdeal.order.dto.response.PurchaseRequestResponseDto;
import com.cropdeal.order.service.PurchaseRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/purchase-requests")
@Tag(name = "Purchase Request Management", description = "APIs for creating, viewing and managing purchase requests")
public class PurchaseRequestController {

	private final PurchaseRequestService purchaseRequestService;

	public PurchaseRequestController(PurchaseRequestService purchaseRequestService) {
		this.purchaseRequestService = purchaseRequestService;
	}

	@Operation(summary = "Create a purchase request", description = "Creates a new purchase request for the authenticated dealer.")
	@PostMapping
	public ResponseEntity<PurchaseRequestResponseDto> createPurchaseRequest(
			@Valid @RequestBody CreatePurchaseRequestDto requestDto, @AuthenticationPrincipal Jwt jwt) {

		Long dealerId = ((Number) jwt.getClaim("userId")).longValue();

		PurchaseRequestResponseDto response = purchaseRequestService.createPurchaseRequest(requestDto, dealerId);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(summary = "Get purchase request by ID", description = "Returns a purchase request by its ID.")
	@GetMapping("/{id}")
	public ResponseEntity<PurchaseRequestResponseDto> getPurchaseRequestById(
	        @PathVariable Long id,
	        @AuthenticationPrincipal Jwt jwt) {

	    Long userId = ((Number) jwt.getClaim("userId")).longValue();

	    PurchaseRequestResponseDto response =
	            purchaseRequestService.getPurchaseRequestById(id, userId);

	    return ResponseEntity.ok(response);
	}

	@Operation(summary = "Get farmer purchase requests", description = "Returns purchase requests associated with the specified farmer.")
	@GetMapping("/farmer/{farmerId}")
	public ResponseEntity<List<PurchaseRequestResponseDto>> getPurchaseRequestsByFarmer(
	        @PathVariable Long farmerId,
	        @AuthenticationPrincipal Jwt jwt) {

	    Long userId = ((Number) jwt.getClaim("userId")).longValue();

	    List<PurchaseRequestResponseDto> response =
	            purchaseRequestService.getPurchaseRequestsByFarmer(
	                    farmerId, userId);

	    return ResponseEntity.ok(response);
	}

	@Operation(summary = "Get dealer purchase requests", description = "Returns purchase requests created by the specified dealer.")
	@GetMapping("/dealer/{dealerId}")
	public ResponseEntity<List<PurchaseRequestResponseDto>> getPurchaseRequestsByDealer(
	        @PathVariable Long dealerId,
	        @AuthenticationPrincipal Jwt jwt) {

	    Long userId = ((Number) jwt.getClaim("userId")).longValue();

	    List<PurchaseRequestResponseDto> response =
	            purchaseRequestService.getPurchaseRequestsByDealer(
	                    dealerId, userId);

	    return ResponseEntity.ok(response);
	}

	@Operation(summary = "Accept a purchase request", description = "Accepts a pending purchase request for the authenticated farmer.")
	@PatchMapping("/{id}/accept")
	public ResponseEntity<PurchaseRequestResponseDto> acceptPurchaseRequest(
	        @PathVariable Long id,
	        @AuthenticationPrincipal Jwt jwt) {

	    Long userId = ((Number) jwt.getClaim("userId")).longValue();

	    PurchaseRequestResponseDto response =
	            purchaseRequestService.acceptPurchaseRequest(id, userId);

	    return ResponseEntity.ok(response);
	}

	@Operation(summary = "Reject a purchase request", description = "Rejects a pending purchase request for the authenticated farmer.")
	@PatchMapping("/{id}/reject")
	public ResponseEntity<PurchaseRequestResponseDto> rejectPurchaseRequest(
	        @PathVariable Long id,
	        @AuthenticationPrincipal Jwt jwt) {

	    Long userId = ((Number) jwt.getClaim("userId")).longValue();

	    PurchaseRequestResponseDto response =
	            purchaseRequestService.rejectPurchaseRequest(id, userId);

	    return ResponseEntity.ok(response);
	}

	@Operation(summary = "Cancel a purchase request", description = "Cancels a pending purchase request for the authenticated dealer.")
	@PatchMapping("/{id}/cancel")
	public ResponseEntity<PurchaseRequestResponseDto> cancelPurchaseRequest(
	        @PathVariable Long id,
	        @AuthenticationPrincipal Jwt jwt) {

	    Long userId = ((Number) jwt.getClaim("userId")).longValue();

	    PurchaseRequestResponseDto response =
	            purchaseRequestService.cancelPurchaseRequest(id, userId);

	    return ResponseEntity.ok(response);
	}
}