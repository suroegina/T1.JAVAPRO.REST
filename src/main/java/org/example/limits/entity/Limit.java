package org.example.limits.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Текущий лимит пользователя.
 * Одна строка на пользователя: user_id — первичный ключ, limit_value — лимит.
 */
@Entity
@Table(name = "limits")
public class Limit {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "limit_value", nullable = false)
    private BigDecimal limitValue;

    protected Limit() {
    }

    public Limit(Long userId, BigDecimal limitValue) {
        this.userId = userId;
        this.limitValue = limitValue;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getLimitValue() {
        return limitValue;
    }

    public void setLimitValue(BigDecimal limitValue) {
        this.limitValue = limitValue;
    }
}
