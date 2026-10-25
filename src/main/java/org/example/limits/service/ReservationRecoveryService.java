package org.example.limits.service;

import org.example.limits.config.LimitProperties;
import org.example.limits.entity.LimitReservation;
import org.example.limits.entity.ReservationStatus;
import org.example.limits.repository.LimitRepository;
import org.example.limits.repository.LimitReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Стратегия восстановления лимита: если внешняя операция «зависла» и по
 * резервированию не пришло ни confirm, ни cancel за время TTL — автоматически
 * отменяем резервирование и возвращаем ранее списанную сумму.
 */
@Component
public class ReservationRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(ReservationRecoveryService.class);

    private final LimitRepository limitRepository;
    private final LimitReservationRepository reservationRepository;
    private final LimitProperties properties;

    public ReservationRecoveryService(LimitRepository limitRepository,
                                      LimitReservationRepository reservationRepository,
                                      LimitProperties properties) {
        this.limitRepository = limitRepository;
        this.reservationRepository = reservationRepository;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.limit.recovery-interval:60000}")
    @Transactional
    public void recoverStaleReservations() {
        Instant cutoff = Instant.now().minus(properties.getReservationTtl());
        List<LimitReservation> stale =
                reservationRepository.findByStatusAndCreatedAtBefore(ReservationStatus.RESERVED, cutoff);

        for (LimitReservation reservation : stale) {
            limitRepository.findByIdForUpdate(reservation.getUserId()).ifPresent(limit ->
                    limit.setLimitValue(limit.getLimitValue().add(reservation.getAmount())));
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation.setUpdatedAt(Instant.now());
        }

        if (!stale.isEmpty()) {
            log.info("Recovered {} stale reservation(s)", stale.size());
        }
    }
}
