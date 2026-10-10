package com.cropdeal.order.controller;

import com.cropdeal.order.dto.*;
import com.cropdeal.order.service.OrderSagaService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sagas")
public class OrderSagaController {
	private final OrderSagaService service;

	public OrderSagaController(OrderSagaService s) {
		service = s;
	}

	@PostMapping("/orders/{negotiationId}")
	public ResponseEntity<SagaResponse> start(@PathVariable Long negotiationId,
			@RequestBody SagaPaymentRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.start(negotiationId, request));
	}
}
