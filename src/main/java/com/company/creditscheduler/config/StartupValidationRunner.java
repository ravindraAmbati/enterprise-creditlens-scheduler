package com.company.creditscheduler.config;

import com.company.creditscheduler.authentication.CredentialProvider;
import com.company.creditscheduler.exception.CreditSchedulerException;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import java.net.URI;
import org.quartz.CronExpression;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class StartupValidationRunner implements CommandLineRunner {

    private final SchedulerProperties schedulerProperties;
    private final SecurityProperties securityProperties;
    private final JavaMailSender mailSender;
    private final CredentialProvider credentialProvider;
    private final ResilientExecutor resilientExecutor;

    public StartupValidationRunner(
            SchedulerProperties schedulerProperties,
            SecurityProperties securityProperties,
            JavaMailSender mailSender,
            CredentialProvider credentialProvider,
            ResilientExecutor resilientExecutor
    ) {
        this.schedulerProperties = schedulerProperties;
        this.securityProperties = securityProperties;
        this.mailSender = mailSender;
        this.credentialProvider = credentialProvider;
        this.resilientExecutor = resilientExecutor;
    }

    @Override
    public void run(String... args) {
        for (SchedulerProperties.Job job : schedulerProperties.getJobs()) {
            if (!job.isEnabled()) {
                continue;
            }
            resilientExecutor.executeVoid("scheduler-startup-validation", job, "startup", () -> validateJob(job));
        }
        validateAdConfiguration();
        validateSmtp();
    }

    private void validateJob(SchedulerProperties.Job job) {
        if (!CronExpression.isValidExpression(job.getCron())) {
            throw new CreditSchedulerException("Invalid cron expression for job " + job.getName());
        }
        validateUri(job.getAuthentication().getEndpoint(), "authentication endpoint");
        validateUri(job.getReport().getEndpoint(), "report endpoint");
        var credentials = credentialProvider.getCredentials(job.getCredentialTarget());
        credentials.clear();
    }

    private void validateUri(String endpoint, String label) {
        URI uri = URI.create(endpoint);
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new CreditSchedulerException("Invalid " + label + ": " + endpoint);
        }
    }

    private void validateAdConfiguration() {
        if (!securityProperties.isEnabled()) {
            return;
        }
        if (isBlank(securityProperties.getUrl())
                || isBlank(securityProperties.getBaseDn())
                || isBlank(securityProperties.getUserDnPattern())
                || isBlank(securityProperties.getServiceAccountGroup())) {
            throw new CreditSchedulerException("Invalid AD configuration");
        }
    }

    private void validateSmtp() {
        if (mailSender instanceof JavaMailSenderImpl impl) {
            try {
                impl.testConnection();
            } catch (Exception exception) {
                throw new CreditSchedulerException("SMTP connectivity validation failed", exception);
            }
        }
        if (schedulerProperties.getStartupValidation().isSmtpTestEmailEnabled()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(schedulerProperties.getStartupValidation().getSmtpTestRecipient());
            message.setSubject("CreditLens scheduler startup validation");
            message.setText("SMTP startup validation succeeded.");
            mailSender.send(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
