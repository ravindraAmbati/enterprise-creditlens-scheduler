package com.company.creditscheduler.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    @Min(1)
    private int threadPoolSize = 5;

    @Valid
    private StartupValidation startupValidation = new StartupValidation();

    @Valid
    @NotEmpty
    private List<Job> jobs = new ArrayList<>();

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public StartupValidation getStartupValidation() {
        return startupValidation;
    }

    public void setStartupValidation(StartupValidation startupValidation) {
        this.startupValidation = startupValidation;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public static class StartupValidation {
        private boolean smtpTestEmailEnabled;
        private String smtpTestRecipient;

        public boolean isSmtpTestEmailEnabled() {
            return smtpTestEmailEnabled;
        }

        public void setSmtpTestEmailEnabled(boolean smtpTestEmailEnabled) {
            this.smtpTestEmailEnabled = smtpTestEmailEnabled;
        }

        public String getSmtpTestRecipient() {
            return smtpTestRecipient;
        }

        public void setSmtpTestRecipient(String smtpTestRecipient) {
            this.smtpTestRecipient = smtpTestRecipient;
        }
    }

    public static class Job {
        @NotBlank
        private String name;
        private boolean enabled = true;
        @NotBlank
        private String cron;
        @NotBlank
        private String credentialTarget;
        @Valid
        private Endpoint authentication = new Endpoint();
        @Valid
        private Report report = new Report();
        @Valid
        private Request request = new Request();
        @Valid
        private Retry retry = new Retry();
        @Valid
        private Validation validation = new Validation();
        @Valid
        private ReportInput reportInput = new ReportInput();
        @Valid
        private Notifications notifications = new Notifications();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public String getCredentialTarget() {
            return credentialTarget;
        }

        public void setCredentialTarget(String credentialTarget) {
            this.credentialTarget = credentialTarget;
        }

        public Endpoint getAuthentication() {
            return authentication;
        }

        public void setAuthentication(Endpoint authentication) {
            this.authentication = authentication;
        }

        public Report getReport() {
            return report;
        }

        public void setReport(Report report) {
            this.report = report;
        }

        public Request getRequest() {
            return request;
        }

        public void setRequest(Request request) {
            this.request = request;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }

        public ReportInput getReportInput() {
            return reportInput;
        }

        public void setReportInput(ReportInput reportInput) {
            this.reportInput = reportInput;
        }

        public Notifications getNotifications() {
            return notifications;
        }

        public void setNotifications(Notifications notifications) {
            this.notifications = notifications;
        }
    }

    public static class Endpoint {
        @NotBlank
        private String endpoint;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public static class Report {
        @NotBlank
        private String endpoint;
        @NotBlank
        private String parentFilePath;
        @NotBlank
        private String filePath;
        @NotBlank
        private String fileName;
        @NotBlank
        private String fileType;
        @NotBlank
        private String generatedReportBasePath;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getParentFilePath() {
            return parentFilePath;
        }

        public void setParentFilePath(String parentFilePath) {
            this.parentFilePath = parentFilePath;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getGeneratedReportBasePath() {
            return generatedReportBasePath;
        }

        public void setGeneratedReportBasePath(String generatedReportBasePath) {
            this.generatedReportBasePath = generatedReportBasePath;
        }
    }

    public static class Request {
        @Min(1)
        private int timeoutSeconds = 60;

        public int getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    public static class Retry {
        @Min(1)
        private int maxAttempts = 5;
        @Min(1)
        private long initialDelayMs = 2000;
        private double multiplier = 2.0;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelayMs() {
            return initialDelayMs;
        }

        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class Validation {
        @Min(0)
        private int waitBeforeValidationSeconds = 10;
        @Min(1)
        private int maxSearchWindowSeconds = 120;
        @Min(0)
        private long minimumFileSizeKb = 5;
        private boolean failIfMultipleFilesFound = true;

        public int getWaitBeforeValidationSeconds() {
            return waitBeforeValidationSeconds;
        }

        public void setWaitBeforeValidationSeconds(int waitBeforeValidationSeconds) {
            this.waitBeforeValidationSeconds = waitBeforeValidationSeconds;
        }

        public int getMaxSearchWindowSeconds() {
            return maxSearchWindowSeconds;
        }

        public void setMaxSearchWindowSeconds(int maxSearchWindowSeconds) {
            this.maxSearchWindowSeconds = maxSearchWindowSeconds;
        }

        public long getMinimumFileSizeKb() {
            return minimumFileSizeKb;
        }

        public void setMinimumFileSizeKb(long minimumFileSizeKb) {
            this.minimumFileSizeKb = minimumFileSizeKb;
        }

        public boolean isFailIfMultipleFilesFound() {
            return failIfMultipleFilesFound;
        }

        public void setFailIfMultipleFilesFound(boolean failIfMultipleFilesFound) {
            this.failIfMultipleFilesFound = failIfMultipleFilesFound;
        }
    }

    public static class ReportInput {
        @NotBlank
        private String reportId;
        private boolean isPortfolio;
        private List<Object> reportParams = new ArrayList<>();

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public boolean isPortfolio() {
            return isPortfolio;
        }

        public void setPortfolio(boolean portfolio) {
            isPortfolio = portfolio;
        }

        public List<Object> getReportParams() {
            return reportParams;
        }

        public void setReportParams(List<Object> reportParams) {
            this.reportParams = reportParams;
        }
    }

    public static class Notifications {
        private List<String> businessEmails = new ArrayList<>();
        private List<String> itSupportEmails = new ArrayList<>();

        public List<String> getBusinessEmails() {
            return businessEmails;
        }

        public void setBusinessEmails(List<String> businessEmails) {
            this.businessEmails = businessEmails;
        }

        public List<String> getItSupportEmails() {
            return itSupportEmails;
        }

        public void setItSupportEmails(List<String> itSupportEmails) {
            this.itSupportEmails = itSupportEmails;
        }
    }
}
