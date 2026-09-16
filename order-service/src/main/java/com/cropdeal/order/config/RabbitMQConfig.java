package com.cropdeal.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PAYMENT_EXCHANGE =
            "cropdeal.payment.exchange";

    public static final String PAYMENT_QUEUE =
            "cropdeal.order.payment.queue";

    public static final String PAYMENT_ROUTING_KEY =
            "payment.status";

    @Bean
    public TopicExchange paymentExchange() {

        return new TopicExchange(
                PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue() {

        return new Queue(
                PAYMENT_QUEUE,
                true);
    }

    @Bean
    public Binding paymentBinding(
            Queue paymentQueue,
            TopicExchange paymentExchange) {

        return BindingBuilder
                .bind(paymentQueue)
                .to(paymentExchange)
                .with(PAYMENT_ROUTING_KEY);
    }
}