package org.example.limits.repository;

import org.example.limits.entity.LimitReservation;
import org.example.limits.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LimitReservationRepository extends JpaRepository<LimitReservation, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM LimitReservation r WHERE r.id = :id")
    Optional<LimitReservation> findByIdForUpdate(@Param("id") UUID id);

    /** Зависшие резервирования для стратегии восстановления. */
    List<LimitReservation> findByStatusAndCreatedAtBefore(ReservationStatus status, Instant createdAt);

    /** Массовая смена статуса (например, отмена всех RESERVED при ежедневном сбросе). */
    @Modifying
    @Query("UPDATE LimitReservation r SET r.status = :toStatus, r.updatedAt = :now WHERE r.status = :fromStatus")
    int updateStatus(@Param("fromStatus") ReservationStatus fromStatus,
                     @Param("toStatus") ReservationStatus toStatus,
                     @Param("now") Instant now);
}
