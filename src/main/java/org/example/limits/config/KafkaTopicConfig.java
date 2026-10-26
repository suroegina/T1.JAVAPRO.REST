package org.example.limits.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Объявляем топик Kafka для событий лимитов. Создаётся автоматически
 * при старте приложения (если ещё не существует).
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic}")
    private String topic;

    @Bean
    public NewTopic limitEventsTopic() {
        return TopicBuilder.name(topic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
