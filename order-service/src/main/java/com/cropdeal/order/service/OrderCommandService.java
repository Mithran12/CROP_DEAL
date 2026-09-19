package com.cropdeal.order.service;

import com.cropdeal.order.dto.CounterOfferRequest;
import com.cropdeal.order.dto.DeliveryConfirmationRequest;
import com.cropdeal.order.dto.OrderRequest;
import com.cropdeal.order.dto.OrderResponse;

public interface OrderCommandService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse acceptOrder(Long id);
    OrderResponse rejectOrder(Long id);
    OrderResponse acceptOffer(Long id);
    OrderResponse rejectOffer(Long id);
    OrderResponse counterOffer(Long id, CounterOfferRequest request);
    OrderResponse cancelOrder(Long id);
    void deleteOrder(Long id);
    OrderResponse markPaymentSuccess(Long id);
    OrderResponse markPaymentFailed(Long id);
    OrderResponse completeOrder(Long id);
    OrderResponse createAuctionOrder(OrderRequest request);
    OrderResponse confirmDelivery(Long orderId, DeliveryConfirmationRequest request);
}
