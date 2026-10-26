package org.example.limits.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.limits.kafka.LimitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Продюсер Kafka: сериализует событие лимита в JSON и кладёт его в топик.
 * Ключом сообщения выступает userId — так сообщения одного пользователя
 * попадают в одну партицию и сохраняют порядок.
 */
@Service
public class LimitEventProducer {

    private static final Logger log = LoggerFactory.getLogger(LimitEventProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public LimitEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                              ObjectMapper objectMapper,
                              @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(LimitEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(event.userId()), json);
            log.info("Published limit event to topic '{}': {}", topic, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize limit event: {}", event, e);
        }
    }
}
