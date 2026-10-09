package com.cropdeal.subscription.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cropdeal.subscription.entity.Subscription;
import com.cropdeal.subscription.entity.SubscriptionStatus;
import com.cropdeal.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CropPublishedEventListener {
    private static final Logger log = LoggerFactory.getLogger(CropPublishedEventListener.class);
    private final ObjectMapper objectMapper;
    private final SubscriptionRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public CropPublishedEventListener(ObjectMapper objectMapper, SubscriptionRepository repository, RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMqConfig.CROP_PUBLISHED_QUEUE)
    public void onCropPublished(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Long cropId = event.hasNonNull("cropId") ? event.get("cropId").asLong() : null;
            String category = event.hasNonNull("category") ? event.get("category").asText().toUpperCase() : null;
            if (cropId == null && category == null) {
                log.warn("Ignoring crop published event without cropId/category");
                return;
            }
            var matches = repository.findAll().stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                    .filter(s -> (cropId != null && cropId.equals(s.getCropId())) ||
                                 (category != null && category.equalsIgnoreCase(s.getCategory())))
                    .toList();
            for (Subscription s : matches) {
                String notification = objectMapper.createObjectNode()
                        .put("dealerId", s.getDealerId())
                        .put("subscriptionId", s.getId())
                        .put("cropId", s.getCropId())
                        .put("category", s.getCategory())
                        .put("message", "A subscribed crop has been published")
                        .toString();
                rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.NOTIFICATION_KEY, notification);
            }
            log.info("Processed crop published event; matched {} subscriptions", matches.size());
        } catch (Exception ex) {
            log.error("Failed to process crop published event", ex);
        }
    }
}
