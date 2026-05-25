package com.company.creditscheduler.notifications.email;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import com.company.creditscheduler.reports.discovery.ReportFileDiscoveryService;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Map;
import static java.util.Map.entry;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailNotificationService {

    private static final String FOOTER = "This is an automated system-generated email. Please do not reply.";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ResilientExecutor resilientExecutor;
    private final ReportFileDiscoveryService discoveryService;

    public EmailNotificationService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            ResilientExecutor resilientExecutor,
            ReportFileDiscoveryService discoveryService
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.resilientExecutor = resilientExecutor;
        this.discoveryService = discoveryService;
    }

    public void sendBusinessSuccess(
            SchedulerProperties.Job job,
            ReportFile reportFile,
            String executionId,
            String correlationId
    ) {
        if (job.getNotifications().getBusinessEmails().isEmpty()) {
            return;
        }
        resilientExecutor.executeVoid("smtp-email-sending", job, correlationId, () -> send(
                job.getNotifications().getBusinessEmails().toArray(String[]::new),
                "CreditLens report generated: " + job.getName(),
                "business-success",
                Map.of(
                        "job", job,
                        "reportFile", reportFile,
                        "executionId", executionId,
                        "correlationId", correlationId,
                        "footer", FOOTER
                )));
    }

    public void sendItFailure(
            SchedulerProperties.Job job,
            String failedStage,
            Throwable error,
            String executionId,
            String correlationId
    ) {
        if (job.getNotifications().getItSupportEmails().isEmpty()) {
            return;
        }
        resilientExecutor.executeVoid("smtp-email-sending", job, correlationId, () -> send(
                job.getNotifications().getItSupportEmails().toArray(String[]::new),
                "CreditLens scheduler failure: " + job.getName(),
                "it-failure",
                Map.ofEntries(
                        entry("job", job),
                        entry("failedStage", failedStage),
                        entry("errorDetails", error.getMessage() == null ? error.toString() : error.getMessage()),
                        entry("retryCount", job.getRetry().getMaxAttempts()),
                        entry("executionId", executionId),
                        entry("correlationId", correlationId),
                        entry("failureTimestamp", Instant.now()),
                        entry("serverName", InetAddress.getLocalHost().getHostName()),
                        entry("searchPath", discoveryService.searchPath(job)),
                        entry("validationWindow", job.getValidation().getMaxSearchWindowSeconds()),
                        entry("footer", FOOTER)
                )));
    }

    private void send(String[] recipients, String subject, String template, Map<String, Object> variables) throws Exception {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("email/" + template, context);
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
