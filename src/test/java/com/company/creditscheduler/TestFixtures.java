package com.company.creditscheduler;

import com.company.creditscheduler.config.SchedulerProperties;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static SchedulerProperties.Job job() {
        SchedulerProperties.Job job = new SchedulerProperties.Job();
        job.setName("customer-report");
        job.setEnabled(true);
        job.setCron("0 0 8 * * ?");
        job.setCredentialTarget("CreditLens/customer-report");

        SchedulerProperties.Endpoint authentication = new SchedulerProperties.Endpoint();
        authentication.setEndpoint("https://creditlens.example.com/api/security/authenticate");
        job.setAuthentication(authentication);

        SchedulerProperties.Report report = new SchedulerProperties.Report();
        report.setEndpoint("https://creditlens.example.com/api/reports/generate");
        report.setParentFilePath("reports");
        report.setFilePath("customer");
        report.setFileName("customer-report");
        report.setFileType("xlsx");
        report.setGeneratedReportBasePath("build");
        job.setReport(report);

        SchedulerProperties.Request request = new SchedulerProperties.Request();
        request.setTimeoutSeconds(2);
        job.setRequest(request);

        SchedulerProperties.Retry retry = new SchedulerProperties.Retry();
        retry.setMaxAttempts(2);
        retry.setInitialDelayMs(1);
        retry.setMultiplier(1.0);
        job.setRetry(retry);

        SchedulerProperties.Validation validation = new SchedulerProperties.Validation();
        validation.setWaitBeforeValidationSeconds(0);
        validation.setMaxSearchWindowSeconds(60);
        validation.setMinimumFileSizeKb(1);
        validation.setFailIfMultipleFilesFound(true);
        job.setValidation(validation);

        SchedulerProperties.ReportInput input = new SchedulerProperties.ReportInput();
        input.setReportId("REPORT_001");
        input.setPortfolio(false);
        input.setReportParams(List.of());
        job.setReportInput(input);

        SchedulerProperties.Notifications notifications = new SchedulerProperties.Notifications();
        notifications.setBusinessEmails(List.of("business@example.com"));
        notifications.setItSupportEmails(List.of("it@example.com"));
        job.setNotifications(notifications);
        return job;
    }

    public static SchedulerProperties schedulerProperties(SchedulerProperties.Job... jobs) {
        SchedulerProperties properties = new SchedulerProperties();
        properties.setJobs(List.of(jobs));
        return properties;
    }
}
