package com.cropdeal.order.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	public static final String EXCHANGE = "cropdeal.exchange";
	public static final String NOTIFICATION_ORDER_QUEUE = "notification.order.queue";

	public static final String ORDER_CREATED = "order.created";
	public static final String ORDER_ACCEPTED = "order.accepted";
	public static final String ORDER_REJECTED = "order.rejected";
	public static final String ORDER_CANCELLED = "order.cancelled";
	public static final String ORDER_COMPLETED = "order.completed";
	public static final String ORDER_PAYMENT_PENDING = "order.payment.pending";
	public static final String ORDER_DELIVERY_QR = "order.delivery.qr";
	public static final String ORDER_DELIVERY_CONFIRMED = "order.delivery.confirmed";
	public static final String NEGOTIATION_EVENT = "order.negotiation";

	@Bean
	public DirectExchange cropDealExchange() {
		return new DirectExchange(EXCHANGE);
	}

	@Bean
	public Queue notificationOrderQueue() {
		return new Queue(NOTIFICATION_ORDER_QUEUE, true);
	}

	@Bean
	public Binding orderCreatedBinding() {
		return bindOrderEvent(ORDER_CREATED);
	}

	@Bean
	public Binding orderAcceptedBinding() {
		return bindOrderEvent(ORDER_ACCEPTED);
	}

	@Bean
	public Binding orderRejectedBinding() {
		return bindOrderEvent(ORDER_REJECTED);
	}

	@Bean
	public Binding orderCancelledBinding() {
		return bindOrderEvent(ORDER_CANCELLED);
	}

	@Bean
	public Binding orderCompletedBinding() {
		return bindOrderEvent(ORDER_COMPLETED);
	}

	@Bean
	public Binding orderPaymentPendingBinding() {
		return bindOrderEvent(ORDER_PAYMENT_PENDING);
	}

	@Bean
	public Binding orderDeliveryQrBinding() {
		return bindOrderEvent(ORDER_DELIVERY_QR);
	}

	@Bean
	public Binding orderDeliveryConfirmedBinding() {
		return bindOrderEvent(ORDER_DELIVERY_CONFIRMED);
	}

	@Bean
	public Binding negotiationEventBinding() {
		return bindOrderEvent(NEGOTIATION_EVENT);
	}

	private Binding bindOrderEvent(String routingKey) {
		return BindingBuilder.bind(notificationOrderQueue()).to(cropDealExchange()).with(routingKey);
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
