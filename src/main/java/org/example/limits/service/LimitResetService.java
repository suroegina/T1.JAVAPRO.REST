package org.example.limits.service;

import org.example.limits.config.LimitProperties;
import org.example.limits.entity.ReservationStatus;
import org.example.limits.repository.LimitRepository;
import org.example.limits.repository.LimitReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Ежедневный сброс лимита: каждый день в 00:00 выставляет всем пользователям
 * дефолтное значение и отменяет «висящие» резервирования.
 */
@Component
public class LimitResetService {

    private static final Logger log = LoggerFactory.getLogger(LimitResetService.class);

    private final LimitRepository limitRepository;
    private final LimitReservationRepository reservationRepository;
    private final LimitProperties properties;

    public LimitResetService(LimitRepository limitRepository,
                             LimitReservationRepository reservationRepository,
                             LimitProperties properties) {
        this.limitRepository = limitRepository;
        this.reservationRepository = reservationRepository;
        this.properties = properties;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDaily() {
        // 1. Сброс лимита всех пользователей к дефолтному значению.
        limitRepository.resetAll(properties.getDefaultValue());

        // 2. Отмена всех незавершённых резервирований: их удержания обнуляются сбросом.
        reservationRepository.updateStatus(
                ReservationStatus.RESERVED, ReservationStatus.CANCELLED, Instant.now());

        log.info("Daily limit reset completed: defaultValue={}", properties.getDefaultValue());
    }
}
