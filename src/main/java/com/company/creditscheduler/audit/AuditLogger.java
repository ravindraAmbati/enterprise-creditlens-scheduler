package com.company.creditscheduler.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void externalCommunication(String host, String jobName, String correlationId) {
        AUDIT.info("AUDIT: External communication initiated Host: {} Job: {} CorrelationId: {}",
                host, jobName, correlationId);
    }

    public void jobEvent(String event, String jobName, String executionId, String correlationId) {
        AUDIT.info("AUDIT: {} Job: {} ExecutionId: {} CorrelationId: {}",
                event, jobName, executionId, correlationId);
    }
}
