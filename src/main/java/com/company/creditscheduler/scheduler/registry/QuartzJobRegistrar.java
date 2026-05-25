package com.company.creditscheduler.scheduler.registry;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.scheduler.jobs.CreditLensReportJob;
import jakarta.annotation.PostConstruct;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Component;

@Component
public class QuartzJobRegistrar {

    private final Scheduler scheduler;
    private final SchedulerProperties schedulerProperties;

    public QuartzJobRegistrar(Scheduler scheduler, SchedulerProperties schedulerProperties) {
        this.scheduler = scheduler;
        this.schedulerProperties = schedulerProperties;
    }

    @PostConstruct
    public void registerJobs() throws Exception {
        for (SchedulerProperties.Job job : schedulerProperties.getJobs()) {
            if (!job.isEnabled()) {
                continue;
            }
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(CreditLensReportJob.JOB_NAME_KEY, job.getName());
            var detail = JobBuilder.newJob(CreditLensReportJob.class)
                    .withIdentity(JobKey.jobKey(job.getName(), "creditlens"))
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();
            var trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getName() + "-trigger", "creditlens")
                    .forJob(detail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()))
                    .build();
            scheduler.scheduleJob(detail, trigger);
        }
    }
}
