package com.cropdeal.order.listener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cropdeal.order.event.PaymentEvent;
import com.cropdeal.order.enums.PaymentStatus;
import com.cropdeal.order.service.OrderService;

@ExtendWith(MockitoExtension.class)
class PaymenteventListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymenteventListener paymentEventListener;

    @Test
    void handlePaymentEvent_shouldPassPaidEventToOrderService() {

        PaymentEvent paymentEvent =
                new PaymentEvent(
                        1L,
                        "PAID",
                        new BigDecimal("5000.00"));

        paymentEventListener.handlePaymentEvent(paymentEvent);

        verify(orderService).handlePaymentEvent(
                1L,
                PaymentStatus.PAID,
                new BigDecimal("5000.00"));
    }

    @Test
    void handlePaymentEvent_shouldRejectInvalidPaymentStatus() {

        PaymentEvent paymentEvent =
                new PaymentEvent(
                        1L,
                        "INVALID_STATUS",
                        new BigDecimal("5000.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentEventListener
                        .handlePaymentEvent(paymentEvent));
    }
    @Test
    void handlePaymentEvent_shouldPassFailedEventToOrderService() {

        PaymentEvent paymentEvent =
                new PaymentEvent(
                        1L,
                        "FAILED",
                        new BigDecimal("5000.00"));

        paymentEventListener.handlePaymentEvent(
                paymentEvent);

        verify(orderService)
                .handlePaymentEvent(
                        1L,
                        PaymentStatus.FAILED,
                        new BigDecimal("5000.00"));
    }
}