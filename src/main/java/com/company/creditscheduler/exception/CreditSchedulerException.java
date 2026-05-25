package com.company.creditscheduler.exception;

public class CreditSchedulerException extends RuntimeException {

    public CreditSchedulerException(String message) {
        super(message);
    }

    public CreditSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}
