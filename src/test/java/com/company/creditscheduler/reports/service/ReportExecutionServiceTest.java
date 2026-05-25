package com.company.creditscheduler.reports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.audit.AuditLogger;
import com.company.creditscheduler.authentication.CreditLensAuthenticationClient;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.metrics.SchedulerMetrics;
import com.company.creditscheduler.notifications.email.EmailNotificationService;
import com.company.creditscheduler.reports.discovery.ReportFileDiscoveryService;
import com.company.creditscheduler.reports.validation.ReportFileValidationService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportExecutionServiceTest {

    @Mock
    private CreditLensAuthenticationClient authenticationClient;
    @Mock
    private CreditLensReportClient reportClient;
    @Mock
    private ReportFileDiscoveryService discoveryService;
    @Mock
    private ReportFileValidationService validationService;
    @Mock
    private EmailNotificationService emailNotificationService;
    @Mock
    private SchedulerMetrics metrics;
    @Mock
    private AuditLogger auditLogger;

    private ReportExecutionService service;

    @BeforeEach
    void setUp() {
        service = new ReportExecutionService(
                authenticationClient,
                reportClient,
                discoveryService,
                validationService,
                emailNotificationService,
                metrics,
                auditLogger);
    }

    @Test
    void executesFullWorkflowAndSendsBusinessSuccessNotification() {
        var job = TestFixtures.job();
        var reportFile = new ReportFile(Path.of("report.xlsx"), "report.xlsx", 2048, "xlsx", Instant.now());
        when(authenticationClient.authenticate(eq(job), anyString())).thenReturn("token");
        when(discoveryService.discover(eq(job), any(Instant.class), anyString())).thenReturn(reportFile);

        var result = service.execute(job);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.jobName()).isEqualTo("customer-report");
        verify(reportClient).generate(eq(job), eq("token"), anyString());
        verify(validationService).validate(eq(job), eq(reportFile), anyString());
        verify(emailNotificationService).sendBusinessSuccess(eq(job), eq(reportFile), anyString(), anyString());
        verify(metrics).incrementActiveJobs();
        verify(metrics).decrementActiveJobs();
        verify(metrics).recordFileDiscoveryLatency(eq("customer-report"), any(Duration.class));
        verify(metrics).recordJobDuration(eq("customer-report"), any(Duration.class));
    }

    @Test
    void sendsItFailureNotificationWhenWorkflowFails() {
        var job = TestFixtures.job();
        when(authenticationClient.authenticate(eq(job), anyString())).thenThrow(new IllegalStateException("auth failed"));

        var result = service.execute(job);

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.message()).isEqualTo("auth failed");
        verify(metrics).failedJob("customer-report");
        verify(emailNotificationService).sendItFailure(eq(job), eq("workflow"), any(IllegalStateException.class), anyString(), anyString());
        verify(metrics).decrementActiveJobs();
        verify(metrics).recordJobDuration(eq("customer-report"), any(Duration.class));
    }

    @Test
    void recordsSmtpFailureWhenItFailureNotificationCannotBeSent() {
        var job = TestFixtures.job();
        when(authenticationClient.authenticate(eq(job), anyString())).thenThrow(new IllegalStateException("auth failed"));
        doThrow(new IllegalStateException("smtp failed"))
                .when(emailNotificationService)
                .sendItFailure(eq(job), eq("workflow"), any(IllegalStateException.class), anyString(), anyString());

        var result = service.execute(job);

        assertThat(result.status()).isEqualTo("FAILED");
        verify(metrics).smtpFailure("customer-report");
    }
}
