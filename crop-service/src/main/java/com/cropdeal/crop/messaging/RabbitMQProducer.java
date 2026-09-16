package com.cropdeal.crop.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.cropdeal.crop.config.RabbitMQConfig;
import com.cropdeal.crop.dto.event.CropPublishedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendCropPublishedEvent(CropPublishedEvent event) {

        rabbitTemplate.convertAndSend(RabbitMQConfig.CROP_PUBLISHED_QUEUE, event);
        
        log.info("Crop published event sent to RabbitMQ for crop ID: {}", event.getCropId());
       
    }
}