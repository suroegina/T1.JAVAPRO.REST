package org.example.limits.service;

import org.example.limits.entity.ReservationStatus;
import org.example.limits.kafka.LimitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Отправляет email-уведомление о смене статуса лимита (резерв / списание /
 * отмена). Использует spring-boot-starter-mail (JavaMailSender).
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String recipient;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${app.notification.from}") String from,
                               @Value("${app.notification.recipient}") String recipient) {
        this.mailSender = mailSender;
        this.from = from;
        this.recipient = recipient;
    }

    public void sendStatusChange(LimitEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("Лимит: " + describeStatus(event.status())
                + " (пользователь " + event.userId() + ")");
        message.setText(buildBody(event));

        try {
            mailSender.send(message);
            log.info("Email notification sent to {} for event {}", recipient, event.reservationId());
        } catch (Exception e) {
            log.error("Failed to send email notification for event {}", event.reservationId(), e);
        }
    }

    private String buildBody(LimitEvent event) {
        return String.format(
                "Изменение статуса лимита.%n%n"
                        + "Пользователь: %d%n"
                        + "Операция: %s%n"
                        + "Сумма: %s%n"
                        + "Резервирование: %s%n"
                        + "Доступный лимит: %s%n"
                        + "Время: %s",
                event.userId(),
                describeStatus(event.status()),
                event.amount(),
                event.reservationId(),
                event.limitValue() == null ? "н/д" : event.limitValue(),
                event.timestamp());
    }

    private String describeStatus(ReservationStatus status) {
        return switch (status) {
            case RESERVED -> "резерв";
            case CONFIRMED -> "списание";
            case CANCELLED -> "отмена";
        };
    }
}
