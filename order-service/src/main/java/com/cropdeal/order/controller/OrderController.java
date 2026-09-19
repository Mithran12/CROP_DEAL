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
import com.cropdeal.order.dto.CounterOfferRequest;
import com.cropdeal.order.dto.NegotiationOfferResponse;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Value("${internal.api.key}")
    private String internalApiKey;

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {

        OrderResponse response =orderService.createOrder(request);

        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(orderService.getAllOrders()
        );
    }

    @GetMapping("/dealer/{dealerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByDealer(@PathVariable Long dealerId) {

        return ResponseEntity.ok(orderService.getOrdersByDealer(dealerId));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByFarmer(@PathVariable Long farmerId) {

        return ResponseEntity.ok(orderService.getOrdersByFarmer(farmerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {

        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.acceptOrder(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.rejectOrder(id));
    }

    @PatchMapping("/{id}/offer/accept")
    public ResponseEntity<OrderResponse> acceptOffer(@PathVariable Long id){return ResponseEntity.ok(orderService.acceptOffer(id));}
    @PatchMapping("/{id}/offer/reject")
    public ResponseEntity<OrderResponse> rejectOffer(@PathVariable Long id){return ResponseEntity.ok(orderService.rejectOffer(id));}
    @PostMapping("/{id}/offer/counter")
    public ResponseEntity<OrderResponse> counterOffer(@PathVariable Long id,@Valid @RequestBody CounterOfferRequest request){return ResponseEntity.ok(orderService.counterOffer(id,request));}
    @GetMapping("/{id}/offers")
    public ResponseEntity<List<NegotiationOfferResponse>> getOffers(@PathVariable Long id){return ResponseEntity.ok(orderService.getNegotiationHistory(id));}

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {

        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {

        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/payment-success")
    public ResponseEntity<OrderResponse> markPaymentSuccess(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {

        if (!internalApiKey.equals(internalKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderService.markPaymentSuccess(id));
    }

    @PatchMapping("/{id}/payment-failed")
    public ResponseEntity<OrderResponse> markPaymentFailed(
            @PathVariable Long id,
            @RequestHeader(value = "X-Internal-Key", required = false) String internalKey) {

        if (!internalApiKey.equals(internalKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderService.markPaymentFailed(id));
    }
    @PatchMapping("/{id}/complete")
    public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.completeOrder(id)
        );
    }
    @PostMapping("/internal/auction")
    public ResponseEntity<OrderResponse> createAuctionOrder(
            @RequestHeader("X-Internal-Key") String internalKey,
            @Valid @RequestBody OrderRequest request) {

        if (!internalApiKey.equals(internalKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(orderService.createAuctionOrder(request));
    }
    @PatchMapping("/internal/{id}/payment-failed")
    public ResponseEntity<OrderResponse> markInternalPaymentFailed(@PathVariable Long id,@RequestHeader("X-Internal-Key") String internalKey){
        if(!internalApiKey.equals(internalKey)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(orderService.markPaymentFailed(id));
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<OrderResponse> getInternalOrder(@PathVariable Long id,@RequestHeader("X-Internal-Key") String internalKey){if(!internalApiKey.equals(internalKey))return ResponseEntity.status(HttpStatus.FORBIDDEN).build();return ResponseEntity.ok(orderService.getOrderByIdInternal(id));}

    @GetMapping(value = "/{orderId}/delivery-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getDeliveryQrCode(@PathVariable Long orderId) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(orderService.getDeliveryQrCode(orderId));
    }

    @PostMapping("/{orderId}/confirm-delivery")
    public ResponseEntity<OrderResponse> confirmDelivery(
            @PathVariable Long orderId,
            @Valid @RequestBody DeliveryConfirmationRequest request) {

        return ResponseEntity.ok(
                orderService.confirmDelivery(orderId, request)
        );
    }
}