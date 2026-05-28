package com.company.creditscheduler.notifications.email;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import com.company.creditscheduler.reports.discovery.ReportFileDiscoveryService;
import java.net.InetAddress;
import java.time.Instant;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final String FOOTER = "This is an automated system-generated email. Please do not reply.";

    private final JavaMailSender mailSender;
    private final EmailContentRenderer emailContentRenderer;
    private final ResilientExecutor resilientExecutor;
    private final ReportFileDiscoveryService discoveryService;

    public EmailNotificationService(
            JavaMailSender mailSender,
            EmailContentRenderer emailContentRenderer,
            ResilientExecutor resilientExecutor,
            ReportFileDiscoveryService discoveryService
    ) {
        this.mailSender = mailSender;
        this.emailContentRenderer = emailContentRenderer;
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
                emailContentRenderer.businessSuccess(job, reportFile, executionId, correlationId, FOOTER)));
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
                emailContentRenderer.itFailure(
                        job,
                        failedStage,
                        error.getMessage() == null ? error.toString() : error.getMessage(),
                        job.getRetry().getMaxAttempts(),
                        executionId,
                        correlationId,
                        Instant.now(),
                        InetAddress.getLocalHost().getHostName(),
                        discoveryService.searchPath(job),
                        job.getValidation().getMaxSearchWindowSeconds(),
                        FOOTER)));
    }

    private void send(String[] recipients, String subject, String html) throws Exception {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);
        helper.setTo(recipients);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
