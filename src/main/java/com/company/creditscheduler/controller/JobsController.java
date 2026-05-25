package com.company.creditscheduler.controller;

import com.company.creditscheduler.dto.JobExecutionResult;
import com.company.creditscheduler.reports.service.ReportExecutionService;
import com.company.creditscheduler.scheduler.registry.JobRegistry;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobsController {

    private final JobRegistry jobRegistry;
    private final ReportExecutionService reportExecutionService;

    public JobsController(JobRegistry jobRegistry, ReportExecutionService reportExecutionService) {
        this.jobRegistry = jobRegistry;
        this.reportExecutionService = reportExecutionService;
    }

    @GetMapping
    public List<Map<String, Object>> listJobs() {
        return jobRegistry.all().stream()
                .map(job -> Map.<String, Object>of(
                        "name", job.getName(),
                        "enabled", job.isEnabled(),
                        "cron", job.getCron(),
                        "reportId", job.getReportInput().getReportId()))
                .toList();
    }

    @GetMapping("/{jobName}")
    public Map<String, Object> getJob(@PathVariable String jobName) {
        var job = jobRegistry.get(jobName);
        return Map.of(
                "name", job.getName(),
                "enabled", job.isEnabled(),
                "cron", job.getCron(),
                "fileType", job.getReport().getFileType(),
                "searchPath", job.getReport().getGeneratedReportBasePath());
    }

    @PostMapping("/{jobName}/trigger")
    public JobExecutionResult trigger(@PathVariable String jobName) {
        return reportExecutionService.execute(jobRegistry.get(jobName));
    }
}
