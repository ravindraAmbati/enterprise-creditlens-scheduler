package com.company.creditscheduler.scheduler.registry;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.exception.CreditSchedulerException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JobRegistry {

    private final SchedulerProperties schedulerProperties;

    public JobRegistry(SchedulerProperties schedulerProperties) {
        this.schedulerProperties = schedulerProperties;
    }

    public List<SchedulerProperties.Job> all() {
        return schedulerProperties.getJobs();
    }

    public SchedulerProperties.Job get(String jobName) {
        return schedulerProperties.getJobs().stream()
                .filter(job -> job.getName().equals(jobName))
                .findFirst()
                .orElseThrow(() -> new CreditSchedulerException("Unknown job: " + jobName));
    }
}
