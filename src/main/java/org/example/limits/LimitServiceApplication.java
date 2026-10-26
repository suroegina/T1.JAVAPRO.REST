package org.example.limits;

import org.example.limits.config.LimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
@EnableConfigurationProperties(LimitProperties.class)
public class LimitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LimitServiceApplication.class, args);
    }
}
