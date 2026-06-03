package com.company.creditscheduler.dto;

import java.time.Instant;

public final class JobExecutionResult {

    private final String jobName;
    private final String executionId;
    private final String correlationId;
    private final String status;
    private final String message;
    private final Instant timestamp;

    public JobExecutionResult(
            String jobName,
            String executionId,
            String correlationId,
            String status,
            String message,
            Instant timestamp
    ) {
        this.jobName = jobName;
        this.executionId = executionId;
        this.correlationId = correlationId;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static JobExecutionResult success(String jobName, String executionId, String correlationId, String message) {
        return new JobExecutionResult(jobName, executionId, correlationId, "SUCCESS", message, Instant.now());
    }

    public static JobExecutionResult failure(String jobName, String executionId, String correlationId, String message) {
        return new JobExecutionResult(jobName, executionId, correlationId, "FAILED", message, Instant.now());
    }

    public String jobName() {
        return jobName;
    }

    public String getJobName() {
        return jobName;
    }

    public String executionId() {
        return executionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String correlationId() {
        return correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String status() {
        return status;
    }

    public String getStatus() {
        return status;
    }

    public String message() {
        return message;
    }

    public String getMessage() {
        return message;
    }

    public Instant timestamp() {
        return timestamp;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
