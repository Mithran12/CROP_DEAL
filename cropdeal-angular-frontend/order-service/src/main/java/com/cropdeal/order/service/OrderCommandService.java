package com.cropdeal.order.service;

import org.springframework.stereotype.Service;

import com.cropdeal.order.dto.DeliveryConfirmationRequest;
import com.cropdeal.order.dto.OrderRequest;
import com.cropdeal.order.dto.OrderResponse;

@Service
public class OrderCommandService {

	private final OrderService delegate;

	public OrderCommandService(OrderService delegate) {
		this.delegate = delegate;
	}

	public OrderResponse create(OrderRequest request) {
		return delegate.createOrder(request);
	}

	public OrderResponse fromNegotiation(Long orderId) {
		return delegate.createOrderFromNegotiation(orderId);
	}

	public OrderResponse fromAuction(Long auctionId) {
		return delegate.createOrderFromAuction(auctionId);
	}

	public OrderResponse accept(Long orderId) {
		return delegate.acceptOrder(orderId);
	}

	public OrderResponse reject(Long orderId) {
		return delegate.rejectOrder(orderId);
	}

	public OrderResponse cancel(Long orderId) {
		return delegate.cancelOrder(orderId);
	}

	public OrderResponse paymentSuccess(Long orderId) {
		return delegate.markPaymentSuccess(orderId);
	}

	public OrderResponse paymentFailed(Long orderId) {
		return delegate.markPaymentFailed(orderId);
	}

	public OrderResponse complete(Long orderId) {
		return delegate.completeOrder(orderId);
	}

	public OrderResponse confirmDelivery(Long orderId, DeliveryConfirmationRequest request) {
		return delegate.confirmDelivery(orderId, request);
	}

	public String getDeliveryQrToken(Long orderId) {
		return delegate.getDeliveryQrToken(orderId);
	}

	public byte[] getDeliveryQrCode(Long orderId) {
		return delegate.getDeliveryQrCode(orderId);
	}
}
