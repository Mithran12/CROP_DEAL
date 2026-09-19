package com.cropdeal.order.service;

import java.util.List;

import com.cropdeal.order.dto.DeliveryConfirmationRequest;
import com.cropdeal.order.dto.OrderRequest;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.dto.CounterOfferRequest;
import com.cropdeal.order.dto.NegotiationOfferResponse;
import com.cropdeal.order.entity.OrderStatus;

public interface OrderService extends OrderCommandService, OrderQueryService {
	OrderResponse createOrder(OrderRequest request);

    OrderResponse createAuctionOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByIdInternal(Long id);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByDealer(Long dealerId);

    List<OrderResponse> getOrdersByFarmer(Long farmerId);

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    OrderResponse acceptOrder(Long id);

    OrderResponse rejectOrder(Long id);

    OrderResponse cancelOrder(Long id);

    OrderResponse acceptOffer(Long id);
    OrderResponse rejectOffer(Long id);
    OrderResponse counterOffer(Long id, CounterOfferRequest request);
    java.util.List<NegotiationOfferResponse> getNegotiationHistory(Long id);
    
    OrderResponse markPaymentSuccess(Long id);

    OrderResponse markPaymentFailed(Long id);

    void deleteOrder(Long id);
    
    OrderResponse completeOrder(Long id);
    
    OrderResponse confirmDelivery(Long orderId, DeliveryConfirmationRequest request);

    byte[] getDeliveryQrCode(Long orderId);
}
