package org.example.limits.service;

import org.example.limits.config.LimitProperties;
import org.example.limits.dto.LimitResponse;
import org.example.limits.dto.ReservationResponse;
import org.example.limits.entity.Limit;
import org.example.limits.entity.LimitReservation;
import org.example.limits.entity.ReservationStatus;
import org.example.limits.exception.LimitException;
import org.example.limits.kafka.LimitEvent;
import org.example.limits.repository.LimitRepository;
import org.example.limits.repository.LimitReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Основная бизнес-логика сервиса лимитов: чтение, резервирование,
 * подтверждение и отмена (восстановление).
 */
@Service
public class LimitService {

    private final LimitRepository limitRepository;
    private final LimitReservationRepository reservationRepository;
    private final LimitProperties properties;
    private final LimitEventProducer eventProducer;

    public LimitService(LimitRepository limitRepository,
                        LimitReservationRepository reservationRepository,
                        LimitProperties properties,
                        LimitEventProducer eventProducer) {
        this.limitRepository = limitRepository;
        this.reservationRepository = reservationRepository;
        this.properties = properties;
        this.eventProducer = eventProducer;
    }

    /**
     * Текущий лимит пользователя. Если пользователя нет в БД — автоматически
     * создаём запись с дефолтным значением.
     */
    @Transactional
    public LimitResponse getLimit(Long userId) {
        Limit limit = limitRepository.findById(userId)
                .orElseGet(() -> createLimit(userId));
        return new LimitResponse(limit.getUserId(), limit.getLimitValue());
    }

    /**
     * Резервирование лимита: удерживаем сумму на время внешней операции.
     * Уменьшает доступный лимит и создаёт запись резервирования в статусе RESERVED.
     */
    @Transactional
    public ReservationResponse reserve(Long userId, BigDecimal amount) {
        validateAmount(amount);

        Limit limit = getOrCreateForUpdate(userId);
        if (limit.getLimitValue().compareTo(amount) < 0) {
            throw new LimitException(400, "INSUFFICIENT_LIMIT",
                    "Insufficient limit for user " + userId
                            + ": available=" + limit.getLimitValue() + ", requested=" + amount);
        }
        limit.setLimitValue(limit.getLimitValue().subtract(amount));

        Instant now = Instant.now();
        LimitReservation reservation = new LimitReservation(
                UUID.randomUUID(), userId, amount, ReservationStatus.RESERVED, now, now);
        reservationRepository.save(reservation);

        eventProducer.publish(new LimitEvent(
                reservation.getId(), userId, amount,
                ReservationStatus.RESERVED, limit.getLimitValue(), now));

        return new ReservationResponse(reservation.getId(), userId, amount,
                ReservationStatus.RESERVED, limit.getLimitValue());
    }

    /**
     * Подтверждение резервирования: внешняя операция завершилась успешно,
     * списание лимита становится окончательным.
     */
    @Transactional
    public ReservationResponse confirm(UUID reservationId) {
        LimitReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new LimitException(404, "RESERVATION_NOT_FOUND",
                        "Reservation not found: " + reservationId));
        ensureReserved(reservation);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setUpdatedAt(Instant.now());

        BigDecimal current = limitRepository.findById(reservation.getUserId())
                .map(Limit::getLimitValue)
                .orElseThrow(() -> new LimitException(500, "LIMIT_NOT_FOUND",
                        "Limit for user " + reservation.getUserId() + " not found"));

        eventProducer.publish(new LimitEvent(
                reservation.getId(), reservation.getUserId(), reservation.getAmount(),
                ReservationStatus.CONFIRMED, current, Instant.now()));

        return new ReservationResponse(reservation.getId(), reservation.getUserId(),
                reservation.getAmount(), reservation.getStatus(), current);
    }

    /**
     * Отмена резервирования: операция не завершилась успешно — восстанавливаем
     * ранее списанный лимит (возвращаем сумму).
     */
    @Transactional
    public ReservationResponse cancel(UUID reservationId) {
        LimitReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new LimitException(404, "RESERVATION_NOT_FOUND",
                        "Reservation not found: " + reservationId));
        ensureReserved(reservation);

        Limit limit = limitRepository.findByIdForUpdate(reservation.getUserId())
                .orElseThrow(() -> new LimitException(500, "LIMIT_NOT_FOUND",
                        "Limit for user " + reservation.getUserId() + " not found"));
        limit.setLimitValue(limit.getLimitValue().add(reservation.getAmount()));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(Instant.now());

        eventProducer.publish(new LimitEvent(
                reservation.getId(), reservation.getUserId(), reservation.getAmount(),
                ReservationStatus.CANCELLED, limit.getLimitValue(), Instant.now()));

        return new ReservationResponse(reservation.getId(), reservation.getUserId(),
                reservation.getAmount(), reservation.getStatus(), limit.getLimitValue());
    }

    private void ensureReserved(LimitReservation reservation) {
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new LimitException(409, "INVALID_STATE",
                    "Reservation " + reservation.getId() + " is already " + reservation.getStatus());
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LimitException(400, "INVALID_AMOUNT", "Amount must be positive");
        }
    }

    private Limit getOrCreateForUpdate(Long userId) {
        return limitRepository.findByIdForUpdate(userId)
                .orElseGet(() -> createLimit(userId));
    }

    /**
     * Создаёт запись лимита с дефолтным значением. Обрабатывает гонку: если другая
     * транзакция уже создала запись, перечитываем её с блокировкой.
     */
    private Limit createLimit(Long userId) {
        try {
            return limitRepository.saveAndFlush(new Limit(userId, properties.getDefaultValue()));
        } catch (DataIntegrityViolationException e) {
            return limitRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new LimitException(500, "LIMIT_NOT_FOUND",
                            "Failed to create limit for user " + userId));
        }
    }
}
