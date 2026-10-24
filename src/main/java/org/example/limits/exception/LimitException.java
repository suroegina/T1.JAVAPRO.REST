package org.example.limits.exception;

/**
 * Доменное исключение сервиса лимитов. Несёт HTTP-статус и семантический код.
 */
public class LimitException extends RuntimeException {

    private final int status;
    private final String code;

    public LimitException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
