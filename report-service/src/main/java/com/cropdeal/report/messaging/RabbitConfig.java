package com.cropdeal.report.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "cropdeal.exchange";
    public static final String QUEUE = "report.receipt.queue";
    public static final String ROUTING = "order.completed";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue receiptQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding receiptBinding() {
        return BindingBuilder
                .bind(receiptQueue())
                .to(exchange())
                .with(ROUTING);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}