package com.cropdeal.order.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.cropdeal.order.config.RabbitMQConfig;
import com.cropdeal.order.event.PaymentEvent;
import com.cropdeal.order.service.OrderService;
import com.cropdeal.order.enums.PaymentStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymenteventListener {

    private final OrderService orderService;
    @RabbitListener(
            queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentEvent(
            PaymentEvent paymentEvent) {

        try {
            PaymentStatus paymentStatus =
                    PaymentStatus.valueOf(
                            paymentEvent.getPaymentStatus()
                                    .toUpperCase());

            orderService.handlePaymentEvent(
                    paymentEvent.getOrderId(),
                    paymentStatus,
                    paymentEvent.getAmount());

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Invalid payment status received: "
                            + paymentEvent.getPaymentStatus(),
                    exception);
        }
    }
}