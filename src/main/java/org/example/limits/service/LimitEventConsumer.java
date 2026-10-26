package org.example.limits.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.limits.kafka.LimitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Консьюмер Kafka: слушает топик с событиями лимитов, десериализует JSON
 * обратно в LimitEvent и передаёт его в NotificationService для отправки
 * уведомления на почту.
 */
@Service
public class LimitEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(LimitEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public LimitEventConsumer(ObjectMapper objectMapper,
                              NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEvent(String message) {
        try {
            LimitEvent event = objectMapper.readValue(message, LimitEvent.class);
            log.info("Consumed limit event: {}", event);
            notificationService.sendStatusChange(event);
        } catch (Exception e) {
            log.error("Failed to process limit event from Kafka: {}", message, e);
        }
    }
}
