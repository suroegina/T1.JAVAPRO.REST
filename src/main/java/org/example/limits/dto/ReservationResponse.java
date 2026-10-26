package org.example.limits.dto;

import org.example.limits.entity.ReservationStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Результат операции с резервированием.
 * limitValue — текущий доступный лимит пользователя после операции.
 */
public record ReservationResponse(
        UUID reservationId,
        Long userId,
        BigDecimal amount,
        ReservationStatus status,
        BigDecimal limitValue
) {
}
