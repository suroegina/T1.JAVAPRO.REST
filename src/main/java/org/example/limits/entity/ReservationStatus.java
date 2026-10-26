package org.example.limits.entity;

/**
 * Статус резервирования лимита.
 * RESERVED  — лимит зарезервирован (удерживается на время внешней операции);
 * CONFIRMED — внешняя операция успешна, списание подтверждено;
 * CANCELLED — операция отменена, лимит восстановлен.
 */
public enum ReservationStatus {
    RESERVED,
    CONFIRMED,
    CANCELLED
}
