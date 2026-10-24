package org.example.limits.dto;

import java.math.BigDecimal;

/**
 * Текущий лимит пользователя.
 */
public record LimitResponse(Long userId, BigDecimal limitValue) {
}
