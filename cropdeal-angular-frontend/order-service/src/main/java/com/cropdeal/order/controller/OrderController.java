package com.cropdeal.order.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cropdeal.order.dto.DeliveryConfirmationRequest;
import com.cropdeal.order.dto.OrderRequest;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.service.OrderService;
import com.cropdeal.order.service.OrderCommandService;
import com.cropdeal.order.service.OrderQueryService;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

	@Value("${internal.api.key}")
	private String internalApiKey;

	private final OrderService orderService;
	private final OrderCommandService commandService;
	private final OrderQueryService queryService;

	public OrderController(OrderService orderService, OrderCommandService commandService,
			OrderQueryService queryService) {
		this.orderService = orderService;
		this.commandService = commandService;
		this.queryService = queryService;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {

		OrderResponse response = commandService.create(request);

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PostMapping("/from-negotiation/{negotiationId}")
	public ResponseEntity<OrderResponse> createOrderFromNegotiation(@PathVariable Long negotiationId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commandService.fromNegotiation(negotiationId));
	}

	@PostMapping("/from-auction/{auctionId}")
	public ResponseEntity<OrderResponse> createOrderFromAuction(@PathVariable Long auctionId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commandService.fromAuction(auctionId));
	}

	@GetMapping("/by-auction/{auctionId}")
	public ResponseEntity<OrderResponse> getOrderByAuctionId(@PathVariable Long auctionId) {
		return orderService.getOrderByAuctionId(auctionId).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/{id}")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {

		return ResponseEntity.ok(queryService.get(id));
	}

	@GetMapping("/internal/{id}")
	public ResponseEntity<OrderResponse> getOrderInternal(@PathVariable Long id,
			@RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {
		if (!internalApiKey.equals(internalKey))
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		return ResponseEntity.ok(orderService.getOrderByIdInternal(id));
	}

	@GetMapping
	public ResponseEntity<List<OrderResponse>> getAllOrders() {

		return ResponseEntity.ok(queryService.all());
	}

	@GetMapping("/dealer/{dealerId}")
	public ResponseEntity<List<OrderResponse>> getOrdersByDealer(@PathVariable Long dealerId) {

		return ResponseEntity.ok(queryService.dealer(dealerId));
	}

	@GetMapping("/farmer/{farmerId}")
	public ResponseEntity<List<OrderResponse>> getOrdersByFarmer(@PathVariable Long farmerId) {

		return ResponseEntity.ok(queryService.farmer(farmerId));
	}

	@GetMapping("/status/{status}")
	public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {

		return ResponseEntity.ok(queryService.status(status));
	}

	@PatchMapping("/{id}/accept")
	public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long id) {

		return ResponseEntity.ok(commandService.accept(id));
	}

	@PatchMapping("/{id}/reject")
	public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long id) {

		return ResponseEntity.ok(commandService.reject(id));
	}

	@RequestMapping(value = "/{id}/cancel", method = { RequestMethod.PATCH, RequestMethod.POST })
	public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {

		return ResponseEntity.ok(commandService.cancel(id));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {

		orderService.deleteOrder(id);

		return ResponseEntity.noContent().build();
	}

	@RequestMapping(value = "/{id}/payment-success", method = { RequestMethod.PATCH, RequestMethod.PUT })
	public ResponseEntity<OrderResponse> markPaymentSuccess(@PathVariable Long id,
			@RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {

		if (!internalApiKey.equals(internalKey)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		return ResponseEntity.ok(commandService.paymentSuccess(id));
	}

	@RequestMapping(value = "/{id}/payment-failed", method = { RequestMethod.PATCH, RequestMethod.PUT })
	public ResponseEntity<OrderResponse> markPaymentFailed(@PathVariable Long id,
			@RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {

		if (!internalApiKey.equals(internalKey)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		return ResponseEntity.ok(commandService.paymentFailed(id));
	}

	@PatchMapping("/{id}/complete")
	public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long id) {

		return ResponseEntity.ok(commandService.complete(id));
	}

	@PostMapping("/internal/auction")
	public ResponseEntity<OrderResponse> createAuctionOrder(@RequestHeader("X-Internal-Key") String internalKey,
			@Valid @RequestBody OrderRequest request) {

		if (!internalApiKey.equals(internalKey)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		return ResponseEntity.ok(orderService.createAuctionOrder(request));
	}

	@GetMapping("/{orderId}/delivery-token")
	public ResponseEntity<String> getDeliveryToken(@PathVariable Long orderId) {
		return ResponseEntity.ok(commandService.getDeliveryQrToken(orderId));
	}

	@GetMapping(value = "/{orderId}/delivery-qr", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<byte[]> getDeliveryQrCode(@PathVariable Long orderId) {
		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(commandService.getDeliveryQrCode(orderId));
	}

	@PostMapping("/{orderId}/confirm-delivery")
	public ResponseEntity<OrderResponse> confirmDelivery(@PathVariable Long orderId,
			@Valid @RequestBody DeliveryConfirmationRequest request) {

		return ResponseEntity.ok(commandService.confirmDelivery(orderId, request));
	}
}