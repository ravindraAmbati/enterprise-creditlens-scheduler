package com.company.creditscheduler.notifications.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.creditscheduler.TestFixtures;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ResilientExecutorTest {

    private final ResilientExecutor executor = new ResilientExecutor();

    @Test
    void returnsValueWhenStageSucceedsImmediately() {
        String value = executor.execute("stage", TestFixtures.job(), "corr", () -> "ok");

        assertThat(value).isEqualTo("ok");
    }

    @Test
    void retriesFailureUntilStageSucceeds() {
        AtomicInteger attempts = new AtomicInteger();

        String value = executor.execute("stage", TestFixtures.job(), "corr", () -> {
            if (attempts.incrementAndGet() == 1) {
                throw new IllegalStateException("temporary");
            }
            return "recovered";
        });

        assertThat(value).isEqualTo("recovered");
        assertThat(attempts).hasValue(2);
    }

    @Test
    void throwsAfterRetryLimitIsReached() {
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> executor.execute("stage", TestFixtures.job(), "corr", () -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("boom");
        })).isInstanceOf(ResilientExecutor.StageExecutionException.class)
                .hasMessageContaining("Stage failed after retries: stage");

        assertThat(attempts).hasValue(2);
    }

    @Test
    void failsWhenStageExceedsConfiguredTimeout() {
        var job = TestFixtures.job();
        job.getRequest().setTimeoutSeconds(1);

        assertThatThrownBy(() -> executor.execute("slow-stage", job, "corr", () -> {
            Thread.sleep(1_500);
            return "late";
        })).isInstanceOf(ResilientExecutor.StageExecutionException.class)
                .hasRootCauseInstanceOf(TimeoutException.class);
    }
}
