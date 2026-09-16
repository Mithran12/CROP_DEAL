package com.cropdeal.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cropdeal.order.client.ExternalServiceClientService;
import com.cropdeal.order.dto.request.CreateOrderRequestDto;
import com.cropdeal.order.dto.request.UpdateOrderStatusRequestDto;
import com.cropdeal.order.dto.response.OrderResponseDto;
import com.cropdeal.order.entity.Order;
import com.cropdeal.order.entity.OrderStatusHistory;
import com.cropdeal.order.enums.OrderStatus;
import com.cropdeal.order.enums.PaymentStatus;
import com.cropdeal.order.enums.PickupStatus;
import com.cropdeal.order.exception.BusinessException;
import com.cropdeal.order.exception.ResourceNotFoundException;
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.repository.OrderStatusHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	private final OrderStatusHistoryRepository orderStatusHistoryRepository;
	
	private final ExternalServiceClientService externalServiceClientService;
	// 1. CREATE ORDER

	@Override
	@Transactional
	public OrderResponseDto createOrder(CreateOrderRequestDto requestDto, Long dealerId) {

		// Verify crop exists in Crop Service
		externalServiceClientService.getCropById(
		        requestDto.getCropId());

		// Verify farmer exists in User Service
		externalServiceClientService.getUserById(
		        requestDto.getFarmerId());
		
		Order order = new Order();
		
		order.setOrderNumber("ORD-" + System.currentTimeMillis());

		order.setFarmerId(requestDto.getFarmerId());

		// Dealer ID comes from authenticated user later
		order.setDealerId(dealerId);

		order.setCropId(requestDto.getCropId());

		order.setQuantity(requestDto.getQuantity());

		order.setAgreedPrice(requestDto.getAgreedPrice());

		BigDecimal totalAmount = requestDto.getQuantity().multiply(requestDto.getAgreedPrice());

		order.setTotalAmount(totalAmount);

		order.setStatus(OrderStatus.PAYMENT_PENDING);

		order.setPaymentStatus(PaymentStatus.PENDING);

		order.setPickupStatus(PickupStatus.PENDING);

		LocalDateTime now = LocalDateTime.now();

		order.setCreatedAt(now);
		order.setUpdatedAt(now);

		Order savedOrder = orderRepository.save(order);

		saveStatusHistory(savedOrder.getId(), null, OrderStatus.PAYMENT_PENDING, dealerId, "Order created");

		return mapToResponseDto(savedOrder);
	}

	// 2. GET ORDER BY ID

	@Override
	public OrderResponseDto getOrderById(Long id) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		return mapToResponseDto(order);
	}

	// 3. GET ORDERS BY FARMER

	@Override
	public List<OrderResponseDto> getOrdersByFarmer(Long farmerId) {

		return orderRepository.findByFarmerId(farmerId).stream().map(this::mapToResponseDto).toList();
	}

	// 4. GET ORDERS BY DEALER

	@Override
	public List<OrderResponseDto> getOrdersByDealer(Long dealerId) {

		return orderRepository.findByDealerId(dealerId).stream().map(this::mapToResponseDto).toList();
	}

	// 5. UPDATE ORDER STATUS

	@Override
	@Transactional
	public OrderResponseDto updateOrderStatus(Long id, UpdateOrderStatusRequestDto requestDto, Long changedBy) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		OrderStatus oldStatus = order.getStatus();

		OrderStatus newStatus = requestDto.getStatus();

		validateStatusTransition(oldStatus, newStatus);

		order.setStatus(newStatus);

		updatePaymentAndPickupStatus(order, newStatus);

		order.setUpdatedAt(LocalDateTime.now());

		if (newStatus == OrderStatus.COMPLETED) {
			order.setCompletedAt(LocalDateTime.now());
		}

		Order updatedOrder = orderRepository.save(order);

		saveStatusHistory(updatedOrder.getId(), oldStatus, newStatus, changedBy, requestDto.getReason());

		return mapToResponseDto(updatedOrder);
	}

	// 6. CANCEL ORDER

	@Override
	@Transactional
	public OrderResponseDto cancelOrder(Long id, Long changedBy) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		OrderStatus oldStatus = order.getStatus();

		if (oldStatus == OrderStatus.COMPLETED) {
			throw new BusinessException("Completed order cannot be cancelled");
		}

		if (oldStatus == OrderStatus.CANCELLED) {
			throw new BusinessException("Order is already cancelled");
		}

		order.setStatus(OrderStatus.CANCELLED);

		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);

		saveStatusHistory(updatedOrder.getId(), oldStatus, OrderStatus.CANCELLED, changedBy, "Order cancelled");

		return mapToResponseDto(updatedOrder);
	}

	// 7. COMPLETE ORDER

	@Override
	@Transactional
	public OrderResponseDto completeOrder(Long id, Long changedBy) {

		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

		OrderStatus oldStatus = order.getStatus();

		if (oldStatus != OrderStatus.PICKED_UP) {
			throw new BusinessException("Only picked-up orders can be completed");
		}

		order.setStatus(OrderStatus.COMPLETED);

		order.setCompletedAt(LocalDateTime.now());

		order.setUpdatedAt(LocalDateTime.now());

		Order updatedOrder = orderRepository.save(order);

		saveStatusHistory(updatedOrder.getId(), oldStatus, OrderStatus.COMPLETED, changedBy, "Order completed");

		return mapToResponseDto(updatedOrder);
	}
	
	// 8. HANDLE PAYMENT EVENT

	@Override
	@Transactional
	public OrderResponseDto handlePaymentEvent(
	        Long orderId,
	        PaymentStatus paymentStatus,
	        BigDecimal amount) {

	    Order order = orderRepository.findById(orderId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "Order not found with id: " + orderId));

	    if (paymentStatus == null) {
	        throw new BusinessException(
	                "Payment status cannot be null");
	    }

	    if (amount == null) {
	        throw new BusinessException(
	                "Payment amount cannot be null");
	    }

	    if (order.getTotalAmount().compareTo(amount) != 0) {
	        throw new BusinessException(
	                "Payment amount does not match order total amount");
	    }

	    // Ignore duplicate payment events
	    if (order.getPaymentStatus() == paymentStatus) {
	        return mapToResponseDto(order);
	    }

	    OrderStatus oldStatus = order.getStatus();

	    if (paymentStatus == PaymentStatus.PAID) {

	        if (oldStatus != OrderStatus.PAYMENT_PENDING) {
	            throw new BusinessException(
	                    "Payment cannot be marked as PAID for order status: "
	                            + oldStatus);
	        }

	        order.setPaymentStatus(PaymentStatus.PAID);
	        order.setStatus(OrderStatus.PAID);

	        order.setUpdatedAt(LocalDateTime.now());

	        Order updatedOrder = orderRepository.save(order);

	        saveStatusHistory(
	                updatedOrder.getId(),
	                oldStatus,
	                OrderStatus.PAID,
	                0L,
	                "Payment successful");

	        return mapToResponseDto(updatedOrder);
	    }

	    if (paymentStatus == PaymentStatus.FAILED) {

	        if (oldStatus != OrderStatus.PAYMENT_PENDING) {
	            throw new BusinessException(
	                    "Payment cannot be marked as FAILED for order status: "
	                            + oldStatus);
	        }

	        order.setPaymentStatus(PaymentStatus.FAILED);
	        order.setStatus(OrderStatus.PAYMENT_FAILED);

	        order.setUpdatedAt(LocalDateTime.now());

	        Order updatedOrder = orderRepository.save(order);

	        saveStatusHistory(
	                updatedOrder.getId(),
	                oldStatus,
	                OrderStatus.PAYMENT_FAILED,
	                0L,
	                "Payment failed");

	        return mapToResponseDto(updatedOrder);
	    }

	    if (paymentStatus == PaymentStatus.REFUNDED) {

	        order.setPaymentStatus(PaymentStatus.REFUNDED);

	        if (oldStatus != OrderStatus.CANCELLED) {

	            order.setStatus(OrderStatus.CANCELLED);

	            order.setUpdatedAt(LocalDateTime.now());

	            Order updatedOrder = orderRepository.save(order);

	            saveStatusHistory(
	                    updatedOrder.getId(),
	                    oldStatus,
	                    OrderStatus.CANCELLED,
	                    0L,
	                    "Payment refunded");

	            return mapToResponseDto(updatedOrder);
	        }

	        order.setUpdatedAt(LocalDateTime.now());

	        Order updatedOrder = orderRepository.save(order);

	        return mapToResponseDto(updatedOrder);
	    }

	    throw new BusinessException(
	            "Unsupported payment status: " + paymentStatus);
	}

	// STATUS TRANSITION VALIDATION

	private void validateStatusTransition(OrderStatus oldStatus, OrderStatus newStatus) {

		if (oldStatus == null || newStatus == null) {
			throw new BusinessException("Order status cannot be null");
		}

		if (oldStatus == newStatus) {
			throw new BusinessException("Order is already in status: " + oldStatus);
		}

		boolean validTransition = (oldStatus == OrderStatus.PAYMENT_PENDING && newStatus == OrderStatus.PAID)

				|| (oldStatus == OrderStatus.PAYMENT_PENDING && newStatus == OrderStatus.PAYMENT_FAILED)

				|| (oldStatus == OrderStatus.PAID && newStatus == OrderStatus.READY_FOR_PICKUP)

				|| (oldStatus == OrderStatus.READY_FOR_PICKUP && newStatus == OrderStatus.PICKED_UP)

				|| (oldStatus == OrderStatus.PICKED_UP && newStatus == OrderStatus.COMPLETED);

		if (!validTransition) {
			throw new BusinessException("Invalid order status transition from " + oldStatus + " to " + newStatus);
		}
	}

	// PAYMENT / PICKUP STATUS UPDATE

	private void updatePaymentAndPickupStatus(Order order, OrderStatus newStatus) {

		if (newStatus == OrderStatus.PAID) {

			order.setPaymentStatus(PaymentStatus.PAID);
		}

		else if (newStatus == OrderStatus.PAYMENT_FAILED) {

			order.setPaymentStatus(PaymentStatus.FAILED);
		}

		else if (newStatus == OrderStatus.READY_FOR_PICKUP) {

			order.setPickupStatus(PickupStatus.READY_FOR_PICKUP);
		}

		else if (newStatus == OrderStatus.PICKED_UP) {

			order.setPickupStatus(PickupStatus.PICKED_UP);
		}
	}

	// SAVE ORDER STATUS HISTORY

	private void saveStatusHistory(Long orderId, OrderStatus oldStatus, OrderStatus newStatus, Long changedBy,
			String reason) {

		OrderStatusHistory history = new OrderStatusHistory();

		history.setOrderId(orderId);
		history.setOldStatus(oldStatus);
		history.setNewStatus(newStatus);
		history.setChangedBy(changedBy);
		history.setReason(reason);
		history.setTimestamp(LocalDateTime.now());

		orderStatusHistoryRepository.save(history);
	}

	// ENTITY → RESPONSE DTO

	private OrderResponseDto mapToResponseDto(Order order) {

		return OrderResponseDto.builder().id(order.getId()).orderNumber(order.getOrderNumber())
				.farmerId(order.getFarmerId()).dealerId(order.getDealerId()).cropId(order.getCropId())
				.quantity(order.getQuantity()).agreedPrice(order.getAgreedPrice()).totalAmount(order.getTotalAmount())
				.status(order.getStatus()).paymentStatus(order.getPaymentStatus()).pickupStatus(order.getPickupStatus())
				.createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt()).completedAt(order.getCompletedAt())
				.build();
	}
}