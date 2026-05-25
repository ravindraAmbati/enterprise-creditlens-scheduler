package com.company.creditscheduler.dto;

import java.time.Instant;

public record JobExecutionResult(
        String jobName,
        String executionId,
        String correlationId,
        String status,
        String message,
        Instant timestamp
) {

    public static JobExecutionResult success(String jobName, String executionId, String correlationId, String message) {
        return new JobExecutionResult(jobName, executionId, correlationId, "SUCCESS", message, Instant.now());
    }

    public static JobExecutionResult failure(String jobName, String executionId, String correlationId, String message) {
        return new JobExecutionResult(jobName, executionId, correlationId, "FAILED", message, Instant.now());
    }
}
