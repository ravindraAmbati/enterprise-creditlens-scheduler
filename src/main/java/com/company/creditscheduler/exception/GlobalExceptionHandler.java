package com.company.creditscheduler.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CreditSchedulerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleCreditSchedulerException(CreditSchedulerException exception) {
        return Map.of(
                "timestamp", Instant.now(),
                "status", "FAILED",
                "message", exception.getMessage()
        );
    }
}
