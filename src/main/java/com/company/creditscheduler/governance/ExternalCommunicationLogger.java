package com.company.creditscheduler.governance;

import com.company.creditscheduler.audit.AuditLogger;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExternalCommunicationLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalCommunicationLogger.class);
    private final AuditLogger auditLogger;

    public ExternalCommunicationLogger(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public void log(String endpoint, String jobName, String correlationId) {
        URI uri = URI.create(endpoint);
        String host = uri.getHost();
        String schemeHost = uri.getScheme() + "://" + host;
        LOGGER.info("External API host detected: {}", schemeHost);
        auditLogger.externalCommunication(host, jobName, correlationId);
    }
}
