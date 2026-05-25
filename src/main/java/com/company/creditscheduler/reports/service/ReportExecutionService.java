package com.company.creditscheduler.reports.service;

import com.company.creditscheduler.audit.AuditLogger;
import com.company.creditscheduler.authentication.CreditLensAuthenticationClient;
import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.JobExecutionResult;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.metrics.SchedulerMetrics;
import com.company.creditscheduler.notifications.email.EmailNotificationService;
import com.company.creditscheduler.reports.discovery.ReportFileDiscoveryService;
import com.company.creditscheduler.reports.validation.ReportFileValidationService;
import com.company.creditscheduler.util.CorrelationIds;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class ReportExecutionService {

    private static final Logger JOBS = LoggerFactory.getLogger("JOBS");

    private final CreditLensAuthenticationClient authenticationClient;
    private final CreditLensReportClient reportClient;
    private final ReportFileDiscoveryService discoveryService;
    private final ReportFileValidationService validationService;
    private final EmailNotificationService emailNotificationService;
    private final SchedulerMetrics metrics;
    private final AuditLogger auditLogger;

    public ReportExecutionService(
            CreditLensAuthenticationClient authenticationClient,
            CreditLensReportClient reportClient,
            ReportFileDiscoveryService discoveryService,
            ReportFileValidationService validationService,
            EmailNotificationService emailNotificationService,
            SchedulerMetrics metrics,
            AuditLogger auditLogger
    ) {
        this.authenticationClient = authenticationClient;
        this.reportClient = reportClient;
        this.discoveryService = discoveryService;
        this.validationService = validationService;
        this.emailNotificationService = emailNotificationService;
        this.metrics = metrics;
        this.auditLogger = auditLogger;
    }

    public JobExecutionResult execute(SchedulerProperties.Job job) {
        String executionId = CorrelationIds.newExecutionId(job.getName());
        String correlationId = CorrelationIds.newCorrelationId();
        Instant started = Instant.now();
        MDC.put("jobName", job.getName());
        MDC.put("executionId", executionId);
        MDC.put("correlationId", correlationId);
        metrics.incrementActiveJobs();
        auditLogger.jobEvent("Job execution started", job.getName(), executionId, correlationId);
        JOBS.info("Job started jobName={} executionId={} correlationId={}", job.getName(), executionId, correlationId);
        String token = null;
        try {
            token = authenticationClient.authenticate(job, correlationId);
            reportClient.generate(job, token, correlationId);
            Thread.sleep(Duration.ofSeconds(job.getValidation().getWaitBeforeValidationSeconds()).toMillis());
            Instant discoveryStarted = Instant.now();
            ReportFile reportFile = discoveryService.discover(job, started, correlationId);
            metrics.recordFileDiscoveryLatency(job.getName(), Duration.between(discoveryStarted, Instant.now()));
            validationService.validate(job, reportFile, correlationId);
            emailNotificationService.sendBusinessSuccess(job, reportFile, executionId, correlationId);
            JOBS.info("Job completed jobName={} executionId={} correlationId={} file={}",
                    job.getName(), executionId, correlationId, reportFile.path());
            auditLogger.jobEvent("Job execution completed", job.getName(), executionId, correlationId);
            return JobExecutionResult.success(job.getName(), executionId, correlationId, "Report generated and validated");
        } catch (Exception exception) {
            metrics.failedJob(job.getName());
            JOBS.error("Job failed jobName={} executionId={} correlationId={} error={}",
                    job.getName(), executionId, correlationId, exception.getMessage(), exception);
            try {
                emailNotificationService.sendItFailure(job, "workflow", exception, executionId, correlationId);
            } catch (Exception emailException) {
                metrics.smtpFailure(job.getName());
                JOBS.error("IT failure notification failed jobName={} executionId={} correlationId={} error={}",
                        job.getName(), executionId, correlationId, emailException.getMessage(), emailException);
            }
            auditLogger.jobEvent("Job execution failed", job.getName(), executionId, correlationId);
            return JobExecutionResult.failure(job.getName(), executionId, correlationId, exception.getMessage());
        } finally {
            token = null;
            metrics.decrementActiveJobs();
            metrics.recordJobDuration(job.getName(), Duration.between(started, Instant.now()));
            MDC.clear();
        }
    }
}
