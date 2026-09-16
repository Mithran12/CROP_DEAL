package com.cropdeal.order.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.repository.OrderStatusHistoryRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderStatusHistoryRepository orderStatusHistoryRepository;

	@Mock
	private ExternalServiceClientService externalServiceClientService;

	@InjectMocks
	private OrderServiceImpl orderService;

	@Test
	void createOrder_shouldCreateSuccessfully() {

		CreateOrderRequestDto requestDto = new CreateOrderRequestDto();

		requestDto.setFarmerId(201L);
		requestDto.setCropId(101L);
		requestDto.setQuantity(new BigDecimal("100"));
		requestDto.setAgreedPrice(new BigDecimal("50.00"));

		Long dealerId = 301L;

		when(externalServiceClientService.getCropById(anyLong())).thenReturn(new Object());

		when(externalServiceClientService.getUserById(anyLong())).thenReturn(new Object());

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order order = invocation.getArgument(0);

			order.setId(1L);

			return order;
		});

		OrderResponseDto response = orderService.createOrder(requestDto, dealerId);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals(201L, response.getFarmerId());
		assertEquals(301L, response.getDealerId());
		assertEquals(101L, response.getCropId());

		assertEquals(new BigDecimal("100"), response.getQuantity());

		assertEquals(new BigDecimal("50.00"), response.getAgreedPrice());

		assertEquals(new BigDecimal("5000.00"), response.getTotalAmount());

		assertEquals(OrderStatus.PAYMENT_PENDING, response.getStatus());

		assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());

		assertEquals(PickupStatus.PENDING, response.getPickupStatus());

		verify(orderRepository, times(1)).save(any(Order.class));

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void getOrderById_shouldReturnSuccessfully() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		OrderResponseDto response = orderService.getOrderById(1L);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("ORD-1", response.getOrderNumber());

		assertEquals(OrderStatus.PAYMENT_PENDING, response.getStatus());

		verify(orderRepository, times(1)).findById(1L);
	}

	@Test
	void getOrderById_shouldThrowExceptionWhenNotFound() {

		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));

		verify(orderRepository, times(1)).findById(999L);
	}

	@Test
	void getOrdersByFarmer_shouldReturnList() {

		Order order1 = createOrder(1L, OrderStatus.PAID);

		Order order2 = createOrder(2L, OrderStatus.COMPLETED);

		when(orderRepository.findByFarmerId(201L)).thenReturn(List.of(order1, order2));

		List<OrderResponseDto> response = orderService.getOrdersByFarmer(201L);

		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals(1L, response.get(0).getId());
		assertEquals(2L, response.get(1).getId());

		verify(orderRepository, times(1)).findByFarmerId(201L);
	}

	@Test
	void getOrdersByDealer_shouldReturnList() {

		Order order1 = createOrder(1L, OrderStatus.PAID);

		Order order2 = createOrder(2L, OrderStatus.READY_FOR_PICKUP);

		when(orderRepository.findByDealerId(301L)).thenReturn(List.of(order1, order2));

		List<OrderResponseDto> response = orderService.getOrdersByDealer(301L);

		assertNotNull(response);
		assertEquals(2, response.size());
		assertEquals(1L, response.get(0).getId());
		assertEquals(2L, response.get(1).getId());

		verify(orderRepository, times(1)).findByDealerId(301L);
	}

	@Test
	void updateOrderStatus_shouldChangePaymentPendingToPaid() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto();

		requestDto.setStatus(OrderStatus.PAID);
		requestDto.setReason("Payment successful");

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.updateOrderStatus(1L, requestDto, 301L);

		assertNotNull(response);

		assertEquals(OrderStatus.PAID, response.getStatus());

		assertEquals(PaymentStatus.PAID, response.getPaymentStatus());

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void updateOrderStatus_shouldChangePaidToReadyForPickup() {

		Order order = createOrder(1L, OrderStatus.PAID);

		UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto();

		requestDto.setStatus(OrderStatus.READY_FOR_PICKUP);

		requestDto.setReason("Crop ready for pickup");

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.updateOrderStatus(1L, requestDto, 201L);

		assertNotNull(response);

		assertEquals(OrderStatus.READY_FOR_PICKUP, response.getStatus());

		assertEquals(PickupStatus.READY_FOR_PICKUP, response.getPickupStatus());

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void updateOrderStatus_shouldChangeReadyForPickupToPickedUp() {

		Order order = createOrder(1L, OrderStatus.READY_FOR_PICKUP);

		UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto();

		requestDto.setStatus(OrderStatus.PICKED_UP);

		requestDto.setReason("Dealer collected crop");

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.updateOrderStatus(1L, requestDto, 301L);

		assertNotNull(response);

		assertEquals(OrderStatus.PICKED_UP, response.getStatus());

		assertEquals(PickupStatus.PICKED_UP, response.getPickupStatus());

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void updateOrderStatus_shouldThrowExceptionForInvalidTransition() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		UpdateOrderStatusRequestDto requestDto = new UpdateOrderStatusRequestDto();

		requestDto.setStatus(OrderStatus.COMPLETED);

		requestDto.setReason("Invalid transition");

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(RuntimeException.class, () -> orderService.updateOrderStatus(1L, requestDto, 301L));

		verify(orderRepository, never()).save(any(Order.class));

		verify(orderStatusHistoryRepository, never()).save(any(OrderStatusHistory.class));
	}

	@Test
	void cancelOrder_shouldCancelSuccessfully() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.cancelOrder(1L, 301L);

		assertNotNull(response);

		assertEquals(OrderStatus.CANCELLED, response.getStatus());

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void cancelOrder_shouldThrowExceptionWhenAlreadyCompleted() {

		Order order = createOrder(1L, OrderStatus.COMPLETED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L, 301L));

		verify(orderRepository, never()).save(any(Order.class));
	}

	@Test
	void completeOrder_shouldCompletePickedUpOrder() {

		Order order = createOrder(1L, OrderStatus.PICKED_UP);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.completeOrder(1L, 301L);

		assertNotNull(response);

		assertEquals(OrderStatus.COMPLETED, response.getStatus());

		assertNotNull(response.getCompletedAt());

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void completeOrder_shouldThrowExceptionWhenOrderNotPickedUp() {

		Order order = createOrder(1L, OrderStatus.PAID);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(RuntimeException.class, () -> orderService.completeOrder(1L, 301L));

		verify(orderRepository, never()).save(any(Order.class));

		verify(orderStatusHistoryRepository, never()).save(any(OrderStatusHistory.class));
	}

	@Test
	void handlePaymentEvent_shouldMarkOrderAsPaid() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.handlePaymentEvent(1L, PaymentStatus.PAID, new BigDecimal("5000.00"));

		assertNotNull(response);

		assertEquals(OrderStatus.PAID, response.getStatus());

		assertEquals(PaymentStatus.PAID, response.getPaymentStatus());

		verify(orderRepository, times(1)).findById(1L);

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void handlePaymentEvent_shouldMarkOrderAsPaymentFailed() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.handlePaymentEvent(1L, PaymentStatus.FAILED,
				new BigDecimal("5000.00"));

		assertNotNull(response);

		assertEquals(OrderStatus.PAYMENT_FAILED, response.getStatus());

		assertEquals(PaymentStatus.FAILED, response.getPaymentStatus());

		verify(orderRepository, times(1)).findById(1L);

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void handlePaymentEvent_shouldMarkOrderAsRefundedAndCancelled() {

		Order order = createOrder(1L, OrderStatus.PAID);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponseDto response = orderService.handlePaymentEvent(1L, PaymentStatus.REFUNDED,
				new BigDecimal("5000.00"));

		assertNotNull(response);

		assertEquals(OrderStatus.CANCELLED, response.getStatus());

		assertEquals(PaymentStatus.REFUNDED, response.getPaymentStatus());

		verify(orderRepository, times(1)).findById(1L);

		verify(orderRepository, times(1)).save(order);

		verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
	}

	@Test
	void handlePaymentEvent_shouldSaveCorrectPaymentHistory() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

		orderService.handlePaymentEvent(1L, PaymentStatus.PAID, new BigDecimal("5000.00"));

		verify(orderStatusHistoryRepository, times(1)).save(argThat(history -> history.getOrderId().equals(1L)
				&& history.getOldStatus() == OrderStatus.PAYMENT_PENDING && history.getNewStatus() == OrderStatus.PAID
				&& history.getChangedBy().equals(0L) && "Payment successful".equals(history.getReason())));
	}

	@Test
	void handlePaymentEvent_shouldIgnoreDuplicatePaymentEvent() {

		Order order = createOrder(1L, OrderStatus.PAID);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		OrderResponseDto response = orderService.handlePaymentEvent(1L, PaymentStatus.PAID, new BigDecimal("5000.00"));

		assertNotNull(response);

		assertEquals(OrderStatus.PAID, response.getStatus());

		assertEquals(PaymentStatus.PAID, response.getPaymentStatus());

		verify(orderRepository, times(1)).findById(1L);

		verify(orderRepository, never()).save(any(Order.class));

		verify(orderStatusHistoryRepository, never()).save(any(OrderStatusHistory.class));
	}
	
	// This check the incorrect amount reject payment event 
	@Test
	void handlePaymentEvent_shouldRejectIncorrectPaymentAmount() {

		Order order = createOrder(1L, OrderStatus.PAYMENT_PENDING);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		assertThrows(RuntimeException.class,
				() -> orderService.handlePaymentEvent(1L, PaymentStatus.PAID, new BigDecimal("4000.00")));

		verify(orderRepository, times(1)).findById(1L);

		verify(orderRepository, never()).save(any(Order.class));

		verify(orderStatusHistoryRepository, never()).save(any(OrderStatusHistory.class));
	}

	// Order not found handling
	@Test
	void handlePaymentEvent_shouldThrowExceptionWhenOrderNotFound() {

		when(orderRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class,
				() -> orderService.handlePaymentEvent(999L, PaymentStatus.PAID, new BigDecimal("5000.00")));

		verify(orderRepository, times(1)).findById(999L);

		verify(orderRepository, never()).save(any(Order.class));

		verify(orderStatusHistoryRepository, never()).save(any(OrderStatusHistory.class));
	}
	
	// Same status must be deleted
	@Test
	void updateOrderStatus_shouldRejectSameStatus() {

	    Order order =
	            createOrder(
	                    1L,
	                    OrderStatus.PAID);

	    UpdateOrderStatusRequestDto requestDto =
	            new UpdateOrderStatusRequestDto();

	    requestDto.setStatus(OrderStatus.PAID);
	    requestDto.setReason("Same status");

	    when(orderRepository.findById(1L))
	            .thenReturn(Optional.of(order));

	    assertThrows(
	            BusinessException.class,
	            () -> orderService.updateOrderStatus(
	                    1L,
	                    requestDto,
	                    301L));

	    verify(orderRepository, never())
	            .save(any(Order.class));

	    verify(orderStatusHistoryRepository, never())
	            .save(any(OrderStatusHistory.class));
	}
	
	// Completed order can not be transition again
	@Test
	void updateOrderStatus_shouldRejectTransitionFromCompleted() {

	    Order order =
	            createOrder(
	                    1L,
	                    OrderStatus.COMPLETED);

	    UpdateOrderStatusRequestDto requestDto =
	            new UpdateOrderStatusRequestDto();

	    requestDto.setStatus(OrderStatus.PAID);
	    requestDto.setReason("Invalid transition");

	    when(orderRepository.findById(1L))
	            .thenReturn(Optional.of(order));

	    assertThrows(
	            BusinessException.class,
	            () -> orderService.updateOrderStatus(
	                    1L,
	                    requestDto,
	                    301L));

	    verify(orderRepository, never())
	            .save(any(Order.class));

	    verify(orderStatusHistoryRepository, never())
	            .save(any(OrderStatusHistory.class));
	}
	
    // Completed order can not be cancelled
	@Test
	void cancelOrder_shouldRejectCompletedOrder() {

	    Order order =
	            createOrder(
	                    1L,
	                    OrderStatus.COMPLETED);

	    when(orderRepository.findById(1L))
	            .thenReturn(Optional.of(order));

	    assertThrows(
	            BusinessException.class,
	            () -> orderService.cancelOrder(
	                    1L,
	                    301L));

	    verify(orderRepository, never())
	            .save(any(Order.class));

	    verify(orderStatusHistoryRepository, never())
	            .save(any(OrderStatusHistory.class));
	}
	
	// Already cancelled order can not be cancelled again
	@Test
	void cancelOrder_shouldRejectAlreadyCancelledOrder() {

	    Order order =
	            createOrder(
	                    1L,
	                    OrderStatus.CANCELLED);

	    when(orderRepository.findById(1L))
	            .thenReturn(Optional.of(order));

	    assertThrows(
	            BusinessException.class,
	            () -> orderService.cancelOrder(
	                    1L,
	                    301L));

	    verify(orderRepository, never())
	            .save(any(Order.class));

	    verify(orderStatusHistoryRepository, never())
	            .save(any(OrderStatusHistory.class));
	}
	
	// Order cannot be completed before pickup
	@Test
	void completeOrder_shouldRejectOrderNotPickedUp() {

	    Order order =
	            createOrder(
	                    1L,
	                    OrderStatus.PAID);

	    when(orderRepository.findById(1L))
	            .thenReturn(Optional.of(order));

	    assertThrows(
	            BusinessException.class,
	            () -> orderService.completeOrder(
	                    1L,
	                    301L));

	    verify(orderRepository, never())
	            .save(any(Order.class));

	    verify(orderStatusHistoryRepository, never())
	            .save(any(OrderStatusHistory.class));
	}
	
	private Order createOrder(Long id, OrderStatus status) {

		Order order = new Order();

		order.setId(id);
		order.setOrderNumber("ORD-" + id);
		order.setFarmerId(201L);
		order.setDealerId(301L);
		order.setCropId(101L);

		order.setQuantity(new BigDecimal("100"));

		order.setAgreedPrice(new BigDecimal("50.00"));

		order.setTotalAmount(new BigDecimal("5000.00"));

		order.setStatus(status);

		order.setPaymentStatus(status == OrderStatus.PAID ? PaymentStatus.PAID : PaymentStatus.PENDING);

		order.setPickupStatus(status == OrderStatus.READY_FOR_PICKUP ? PickupStatus.READY_FOR_PICKUP
				: status == OrderStatus.PICKED_UP ? PickupStatus.PICKED_UP : PickupStatus.PENDING);

		order.setCreatedAt(LocalDateTime.now());
		order.setUpdatedAt(LocalDateTime.now());

		return order;
	}
}