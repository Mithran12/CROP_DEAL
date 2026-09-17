package com.cropdeal.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.cropdeal.order.dto.request.CreateOrderRequestDto;
import com.cropdeal.order.dto.request.UpdateOrderStatusRequestDto;
import com.cropdeal.order.dto.response.OrderResponseDto;
import com.cropdeal.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(
        name = "Order Management",
        description = "APIs for creating, viewing, updating and completing orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // CREATE ORDER

    @Operation(
            summary = "Create an order",
            description = "Creates a new order for the authenticated dealer after the final price and quantity are agreed.")
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto requestDto,
            @AuthenticationPrincipal Jwt jwt) {

        Long dealerId =
                ((Number) jwt.getClaim("userId"))
                        .longValue();

        OrderResponseDto response =
                orderService.createOrder(
                        requestDto,
                        dealerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ORDER BY ID

    @Operation(
            summary = "Get order by ID",
            description = "Returns an order when the authenticated user is the farmer or dealer associated with the order.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long authenticatedUserId =
                ((Number) jwt.getClaim("userId"))
                        .longValue();

        OrderResponseDto response =
                orderService.getOrderById(
                        id,
                        authenticatedUserId);

        return ResponseEntity.ok(response);
    }

    // GET ORDERS BY FARMER

    @Operation(
            summary = "Get farmer orders",
            description = "Returns all orders belonging to the authenticated farmer.")
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByFarmer(
            @PathVariable Long farmerId,
            @AuthenticationPrincipal Jwt jwt) {

        Long authenticatedUserId =
                ((Number) jwt.getClaim("userId"))
                        .longValue();

        List<OrderResponseDto> response =
                orderService.getOrdersByFarmer(
                        farmerId,
                        authenticatedUserId);

        return ResponseEntity.ok(response);
    }

    // GET ORDERS BY DEALER

    @Operation(
            summary = "Get dealer orders",
            description = "Returns all orders belonging to the authenticated dealer.")
    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByDealer(
            @PathVariable Long dealerId,
            @AuthenticationPrincipal Jwt jwt) {

        Long authenticatedUserId =
                ((Number) jwt.getClaim("userId"))
                        .longValue();

        List<OrderResponseDto> response =
                orderService.getOrdersByDealer(
                        dealerId,
                        authenticatedUserId);

        return ResponseEntity.ok(response);
    }

    // UPDATE ORDER STATUS

    @Operation(
            summary = "Update order status",
            description = "Updates an order according to the allowed order lifecycle transitions.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequestDto requestDto,
            @AuthenticationPrincipal Jwt jwt) {

    	Long changedBy =
    	        ((Number) jwt.getClaim("userId"))
    	                .longValue();

    	String role = jwt.getClaimAsString("role");

    	OrderResponseDto response =
    	        orderService.updateOrderStatus(
    	                id,
    	                requestDto,
    	                changedBy,
    	                role);

        return ResponseEntity.ok(response);
    }

    // CANCEL ORDER

    @Operation(
            summary = "Cancel order",
            description = "Cancels an order when cancellation is allowed by the order lifecycle.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long changedBy =
                ((Number) jwt.getClaim("userId"))
                        .longValue();

        OrderResponseDto response =
                orderService.cancelOrder(
                        id,
                        changedBy);

        return ResponseEntity.ok(response);
    }

    // COMPLETE ORDER

    @Operation(
            summary = "Complete order",
            description = "Marks an order as completed after the crop has been picked up.")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderResponseDto> completeOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long changedBy =
                ((Number) jwt.getClaim("userId"))
                        .longValue();

        OrderResponseDto response =
                orderService.completeOrder(
                        id,
                        changedBy);

        return ResponseEntity.ok(response);
    }
}