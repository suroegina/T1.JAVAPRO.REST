package org.example.limits.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Настройки сервиса лимитов: дефолтное значение лимита и таймауты.
 */
@ConfigurationProperties(prefix = "app.limit")
public class LimitProperties {

    /** Дефолтный лимит, общий для всех пользователей. */
    private BigDecimal defaultValue = new BigDecimal("100000.00");

    /** Сколько резервирование может «висеть» до автоотмены. */
    private Duration reservationTtl = Duration.ofMinutes(10);

    public BigDecimal getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(BigDecimal defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Duration getReservationTtl() {
        return reservationTtl;
    }

    public void setReservationTtl(Duration reservationTtl) {
        this.reservationTtl = reservationTtl;
    }
}
