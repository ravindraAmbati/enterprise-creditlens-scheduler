package com.company.creditscheduler.scheduler.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.exception.CreditSchedulerException;
import org.junit.jupiter.api.Test;

class JobRegistryTest {

    @Test
    void returnsAllConfiguredJobs() {
        var job = TestFixtures.job();
        var registry = new JobRegistry(TestFixtures.schedulerProperties(job));

        assertThat(registry.all()).containsExactly(job);
    }

    @Test
    void returnsJobByName() {
        var job = TestFixtures.job();
        var registry = new JobRegistry(TestFixtures.schedulerProperties(job));

        assertThat(registry.get("customer-report")).isSameAs(job);
    }

    @Test
    void failsWhenJobIsUnknown() {
        var registry = new JobRegistry(TestFixtures.schedulerProperties(TestFixtures.job()));

        assertThatThrownBy(() -> registry.get("missing"))
                .isInstanceOf(CreditSchedulerException.class)
                .hasMessage("Unknown job: missing");
    }
}
