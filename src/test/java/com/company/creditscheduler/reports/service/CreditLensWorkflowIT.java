package com.company.creditscheduler.reports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.audit.AuditLogger;
import com.company.creditscheduler.authentication.CredentialProvider;
import com.company.creditscheduler.authentication.CreditLensAuthenticationClient;
import com.company.creditscheduler.dto.Credentials;
import com.company.creditscheduler.metrics.SchedulerMetrics;
import com.company.creditscheduler.notifications.email.EmailContentRenderer;
import com.company.creditscheduler.notifications.email.EmailNotificationService;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import com.company.creditscheduler.reports.discovery.ReportFileDiscoveryService;
import com.company.creditscheduler.reports.validation.ReportFileValidationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;

class CreditLensWorkflowIT {

    private MockWebServer server;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void executesCreditLensWorkflowFromAuthenticationThroughBusinessNotification() throws Exception {
        var job = TestFixtures.job();
        job.getReport().setGeneratedReportBasePath(tempDir.toString());
        job.getAuthentication().setEndpoint(server.url("/api/security/authenticate").toString());
        job.getReport().setEndpoint(server.url("/api/reports/generate").toString().replaceAll("/$", ""));
        job.getRetry().setMaxAttempts(1);
        job.getValidation().setWaitBeforeValidationSeconds(0);
        job.getValidation().setMinimumFileSizeKb(0);

        Path expectedOutput = tempDir.resolve("reports").resolve("customer").resolve("customer-report.xlsx");
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                try {
                    if ("/api/security/authenticate".equals(request.getPath())) {
                        return json("""
                                {"payLoad":{"user":{"UserId":"clapi"},"token":"test-token","devMode":false},"status":null}
                                """);
                    }
                    if ("/api/reports/generate/customer/customer-report/xlsx".equals(request.getPath())) {
                        Files.createDirectories(expectedOutput.getParent());
                        Files.writeString(expectedOutput, "generated report content");
                        return new MockResponse().setResponseCode(202);
                    }
                    return new MockResponse().setResponseCode(404);
                } catch (IOException exception) {
                    return new MockResponse().setResponseCode(500).setBody(exception.getMessage());
                }
            }
        });

        ResilientExecutor resilientExecutor = new ResilientExecutor();
        CredentialProvider credentialProvider = target -> new Credentials("clapi", "secret".toCharArray());
        var communicationLogger = new com.company.creditscheduler.governance.ExternalCommunicationLogger(new AuditLogger());
        var webClientBuilder = WebClient.builder();
        var authenticationClient = new CreditLensAuthenticationClient(
                webClientBuilder,
                credentialProvider,
                communicationLogger,
                resilientExecutor);
        var reportClient = new CreditLensReportClient(webClientBuilder, communicationLogger, resilientExecutor);
        var discoveryService = new ReportFileDiscoveryService(resilientExecutor);
        var validationService = new ReportFileValidationService(resilientExecutor);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        var emailService = new EmailNotificationService(
                mailSender,
                new EmailContentRenderer(),
                resilientExecutor,
                discoveryService);
        var metrics = new SchedulerMetrics(new SimpleMeterRegistry());
        var executionService = new ReportExecutionService(
                authenticationClient,
                reportClient,
                discoveryService,
                validationService,
                emailService,
                metrics,
                new AuditLogger());

        var result = executionService.execute(job);

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(expectedOutput).exists().content().contains("generated report content");
        verify(mailSender).send(any(MimeMessage.class));

        RecordedRequest authRequest = server.takeRequest();
        RecordedRequest generateRequest = server.takeRequest();
        assertThat(authRequest.getBody().readUtf8()).contains("\"UserName\":\"clapi\"");
        assertThat(generateRequest.getHeader("Authorization")).isEqualTo("Bearer test-token");
        assertThat(generateRequest.getBody().readUtf8()).contains("\"reportId\":\"REPORT_001\"");
    }

    private MockResponse json(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
