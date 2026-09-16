package com.cropdeal.order.service;

import java.math.BigDecimal;
import java.util.List;

import com.cropdeal.order.dto.request.CreateOrderRequestDto;
import com.cropdeal.order.dto.request.UpdateOrderStatusRequestDto;
import com.cropdeal.order.dto.response.OrderResponseDto;
import com.cropdeal.order.enums.PaymentStatus;

public interface OrderService {

	OrderResponseDto createOrder(CreateOrderRequestDto requestDto, Long dealerId);

	OrderResponseDto getOrderById(Long id);

	List<OrderResponseDto> getOrdersByFarmer(Long farmerId);

	List<OrderResponseDto> getOrdersByDealer(Long dealerId);

	OrderResponseDto updateOrderStatus(Long id, UpdateOrderStatusRequestDto requestDto, Long changedBy);

	OrderResponseDto cancelOrder(Long id, Long changedBy);

	OrderResponseDto completeOrder(Long id, Long changedBy);

	OrderResponseDto handlePaymentEvent(Long orderId, PaymentStatus paymentStatus, BigDecimal amount);
}