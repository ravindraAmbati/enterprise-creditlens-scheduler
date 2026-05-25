package com.company.creditscheduler.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class SchedulerMetrics {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeJobs;

    public SchedulerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeJobs = meterRegistry.gauge("creditlens.scheduler.active.jobs", new AtomicInteger(0));
    }

    public void incrementActiveJobs() {
        activeJobs.incrementAndGet();
    }

    public void decrementActiveJobs() {
        activeJobs.decrementAndGet();
    }

    public void failedJob(String jobName) {
        Counter.builder("creditlens.scheduler.failed.jobs")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment();
    }

    public void smtpFailure(String jobName) {
        Counter.builder("creditlens.scheduler.smtp.failures")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment();
    }

    public void recordJobDuration(String jobName, Duration duration) {
        Timer.builder("creditlens.scheduler.job.duration")
                .tag("job", jobName)
                .register(meterRegistry)
                .record(duration);
    }

    public void recordFileDiscoveryLatency(String jobName, Duration duration) {
        Timer.builder("creditlens.scheduler.file.discovery.latency")
                .tag("job", jobName)
                .register(meterRegistry)
                .record(duration);
    }
}
