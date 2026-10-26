package org.example.limits.kafka;

import org.example.limits.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Событие о смене статуса лимита: резерв (RESERVED), списание (CONFIRMED)
 * или отмена (CANCELLED). Именно это продюсер кладёт в топик, а консьюмер
 * читает и передаёт в NotificationService.
 */
public record LimitEvent(
        UUID reservationId,
        Long userId,
        BigDecimal amount,
        ReservationStatus status,
        BigDecimal limitValue,
        Instant timestamp
) {
}
