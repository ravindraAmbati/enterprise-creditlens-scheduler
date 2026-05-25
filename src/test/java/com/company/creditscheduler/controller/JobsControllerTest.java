package com.company.creditscheduler.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.dto.JobExecutionResult;
import com.company.creditscheduler.reports.service.ReportExecutionService;
import com.company.creditscheduler.scheduler.registry.JobRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobsControllerTest {

    @Mock
    private JobRegistry jobRegistry;

    @Mock
    private ReportExecutionService reportExecutionService;

    @Test
    void listsConfiguredJobs() {
        var job = TestFixtures.job();
        when(jobRegistry.all()).thenReturn(java.util.List.of(job));
        var controller = new JobsController(jobRegistry, reportExecutionService);

        var jobs = controller.listJobs();

        assertThat(jobs).singleElement()
                .satisfies(item -> {
                    assertThat(item).containsEntry("name", "customer-report");
                    assertThat(item).containsEntry("enabled", true);
                    assertThat(item).containsEntry("reportId", "REPORT_001");
                });
    }

    @Test
    void returnsJobDetails() {
        var job = TestFixtures.job();
        when(jobRegistry.get("customer-report")).thenReturn(job);
        var controller = new JobsController(jobRegistry, reportExecutionService);

        var details = controller.getJob("customer-report");

        assertThat(details).containsEntry("name", "customer-report")
                .containsEntry("fileType", "xlsx");
    }

    @Test
    void triggersJobExecution() {
        var job = TestFixtures.job();
        var result = JobExecutionResult.success("customer-report", "exec", "corr", "ok");
        when(jobRegistry.get("customer-report")).thenReturn(job);
        when(reportExecutionService.execute(job)).thenReturn(result);
        var controller = new JobsController(jobRegistry, reportExecutionService);

        assertThat(controller.trigger("customer-report")).isSameAs(result);
        verify(reportExecutionService).execute(job);
    }
}
