package org.example.limits.dto;

import java.math.BigDecimal;

/**
 * Запрос на резервирование лимита.
 */
public record ReservationRequest(BigDecimal amount) {
}
