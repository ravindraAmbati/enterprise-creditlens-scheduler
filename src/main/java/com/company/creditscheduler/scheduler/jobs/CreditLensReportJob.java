package com.company.creditscheduler.scheduler.jobs;

import com.company.creditscheduler.reports.service.ReportExecutionService;
import com.company.creditscheduler.scheduler.registry.JobRegistry;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class CreditLensReportJob implements Job {

    public static final String JOB_NAME_KEY = "jobName";

    private final JobRegistry jobRegistry;
    private final ReportExecutionService reportExecutionService;

    public CreditLensReportJob(JobRegistry jobRegistry, ReportExecutionService reportExecutionService) {
        this.jobRegistry = jobRegistry;
        this.reportExecutionService = reportExecutionService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getMergedJobDataMap().getString(JOB_NAME_KEY);
        var job = jobRegistry.get(jobName);
        var result = reportExecutionService.execute(job);
        if (!"SUCCESS".equals(result.status())) {
            throw new JobExecutionException(result.message());
        }
    }
}
