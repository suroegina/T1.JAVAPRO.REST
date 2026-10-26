package org.example.limits.handler;

import org.example.limits.dto.ErrorDto;
import org.example.limits.exception.LimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Превращает исключения сервиса в понятный HTTP-ответ с телом ErrorDto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LimitException.class)
    public ResponseEntity<ErrorDto> handleLimitException(LimitException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ErrorDto(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(500)
                .body(new ErrorDto("INTERNAL_ERROR", "Unexpected server error"));
    }
}
