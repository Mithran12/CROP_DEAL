package com.cropdeal.order.service;

import com.cropdeal.order.dto.NegotiationOfferResponse;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.entity.OrderStatus;

import java.util.List;

public interface OrderQueryService {
    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByIdInternal(Long id);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersByDealer(Long dealerId);
    List<OrderResponse> getOrdersByFarmer(Long farmerId);
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    List<NegotiationOfferResponse> getNegotiationHistory(Long id);
    byte[] getDeliveryQrCode(Long orderId);
}
