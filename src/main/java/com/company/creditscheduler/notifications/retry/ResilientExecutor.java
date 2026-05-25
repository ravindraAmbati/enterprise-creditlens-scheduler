package com.company.creditscheduler.notifications.retry;

import com.company.creditscheduler.config.SchedulerProperties;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResilientExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientExecutor.class);

    public <T> T execute(
            String stage,
            SchedulerProperties.Job job,
            String correlationId,
            Callable<T> callable
    ) {
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(job.getRetry().getMaxAttempts())
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(job.getRetry().getInitialDelayMs()),
                        job.getRetry().getMultiplier()))
                .retryExceptions(Exception.class)
                .build();

        Retry retry = Retry.of(job.getName() + "-" + stage, retryConfig);
        retry.getEventPublisher().onRetry(event -> LOGGER.warn(
                "Retrying stage={} jobName={} attempt={} correlationId={} cause={}",
                stage,
                job.getName(),
                event.getNumberOfRetryAttempts(),
                correlationId,
                event.getLastThrowable() == null ? "unknown" : event.getLastThrowable().getMessage()));

        Callable<T> timed = () -> CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception exception) {
                throw new StageExecutionException(exception);
            }
        }).get(job.getRequest().getTimeoutSeconds(), TimeUnit.SECONDS);

        try {
            return Retry.decorateCallable(retry, timed).call();
        } catch (Exception exception) {
            Throwable cause = exception instanceof StageExecutionException ? exception.getCause() : exception;
            throw new StageExecutionException("Stage failed after retries: " + stage, cause);
        }
    }

    public void executeVoid(
            String stage,
            SchedulerProperties.Job job,
            String correlationId,
            ThrowingRunnable runnable
    ) {
        execute(stage, job, correlationId, () -> {
            runnable.run();
            return null;
        });
    }

    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    public static class StageExecutionException extends RuntimeException {
        public StageExecutionException(String message, Throwable cause) {
            super(message, cause);
        }

        public StageExecutionException(Throwable cause) {
            super(cause);
        }
    }
}
