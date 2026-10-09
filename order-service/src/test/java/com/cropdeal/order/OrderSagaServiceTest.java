package com.cropdeal.order;

import com.cropdeal.order.client.CropServiceClient;
import com.cropdeal.order.client.PaymentClient;
import com.cropdeal.order.dto.OrderResponse;
import com.cropdeal.order.dto.PaymentResponse;
import com.cropdeal.order.dto.SagaPaymentRequest;
import com.cropdeal.order.dto.SagaResponse;
import com.cropdeal.order.entity.Order;
import com.cropdeal.order.entity.OrderStatus;
import com.cropdeal.order.repository.OrderRepository;
import com.cropdeal.order.repository.OrderSagaRepository;
import com.cropdeal.order.service.EventSourcingService;
import com.cropdeal.order.service.OrderSagaService;
import com.cropdeal.order.service.OrderService;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSagaServiceTest {

    @Mock
    OrderService orderService;

    @Mock
    OrderRepository orders;

    @Mock
    CropServiceClient crops;

    @Mock
    PaymentClient payments;

    @Mock
    OrderSagaRepository sagas;

    @Mock
    EventSourcingService events;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void successfulSagaReservesCropAndCompletesPayment() {

        OrderSagaService service = new OrderSagaService(
                orderService,
                orders,
                crops,
                payments,
                sagas,
                events
        );

        OrderResponse response = new OrderResponse();
        response.setId(5L);

        Order order = new Order();
        order.setId(5L);
        order.setCropId(9L);
        order.setQuantity(10.0);
        order.setStatus(OrderStatus.CREATED);

        when(orderService.createOrderFromNegotiation(3L))
                .thenReturn(response);

        when(orders.findById(5L))
                .thenReturn(Optional.of(order));

        when(sagas.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setOrderId(5L);
        paymentResponse.setStatus("SUCCESS");

        when(payments.create(any()))
                .thenReturn(paymentResponse);

        SagaPaymentRequest request = new SagaPaymentRequest();
        request.setPaymentMethod("CASH");

        SagaResponse output = service.start(3L, request);

        assertEquals("PAYMENT_SUCCESS", output.getStatus());

        verify(crops).reduceQuantity(9L, 10.0);

        verify(events, atLeastOnce())
                .append(
                        eq("ORDER"),
                        eq("5"),
                        anyString(),
                        any()
                );
    }
}