package com.cropdeal.subscription.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EXCHANGE = "cropdeal.events";
    public static final String CROP_PUBLISHED_QUEUE = "subscription.crop.published.queue";
    public static final String CROP_PUBLISHED_KEY = "crop.published";
    public static final String NOTIFICATION_KEY = "subscription.notification";

    @Bean
    TopicExchange cropDealExchange() { return new TopicExchange(EXCHANGE, true, false); }

    @Bean
    Queue cropPublishedQueue() { return QueueBuilder.durable(CROP_PUBLISHED_QUEUE).build(); }

    @Bean
    Binding cropPublishedBinding(Queue cropPublishedQueue, TopicExchange cropDealExchange) {
        return BindingBuilder.bind(cropPublishedQueue).to(cropDealExchange).with(CROP_PUBLISHED_KEY);
    }
}
