package org.example.limits.controller;

import org.example.limits.dto.LimitResponse;
import org.example.limits.dto.ReservationRequest;
import org.example.limits.dto.ReservationResponse;
import org.example.limits.service.LimitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/limits")
public class LimitController {

    private final LimitService limitService;

    public LimitController(LimitService limitService) {
        this.limitService = limitService;
    }

    /** Текущий лимит пользователя (при отсутствии — авто-создание с дефолтом). */
    @GetMapping("/{userId}")
    public LimitResponse getLimit(@PathVariable Long userId) {
        return limitService.getLimit(userId);
    }

    /** Резервирование лимита на время внешней операции. */
    @PostMapping("/{userId}/reservations")
    public ReservationResponse reserve(@PathVariable Long userId,
                                       @RequestBody ReservationRequest request) {
        return limitService.reserve(userId, request.amount());
    }

    /** Подтверждение резервирования (операция успешна). */
    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable UUID reservationId) {
        return limitService.confirm(reservationId);
    }

    /** Отмена резервирования (восстановление ранее списанного лимита). */
    @PostMapping("/reservations/{reservationId}/cancel")
    public ReservationResponse cancel(@PathVariable UUID reservationId) {
        return limitService.cancel(reservationId);
    }
}
