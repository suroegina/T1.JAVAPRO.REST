package org.example.limits.repository;

import org.example.limits.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.util.Optional;

public interface LimitRepository extends JpaRepository<Limit, Long> {

    /** Блокируем строку лимита (SELECT ... FOR UPDATE) для атомарного изменения. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Limit l WHERE l.userId = :userId")
    Optional<Limit> findByIdForUpdate(@Param("userId") Long userId);

    /** Ежедневный сброс: всем пользователям выставляем дефолтный лимит. */
    @Modifying
    @Query("UPDATE Limit l SET l.limitValue = :value")
    void resetAll(@Param("value") BigDecimal value);
}
