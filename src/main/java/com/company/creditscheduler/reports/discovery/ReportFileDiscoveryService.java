package com.company.creditscheduler.reports.discovery;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.exception.CreditSchedulerException;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportFileDiscoveryService {

    private final ResilientExecutor resilientExecutor;

    public ReportFileDiscoveryService(ResilientExecutor resilientExecutor) {
        this.resilientExecutor = resilientExecutor;
    }

    public ReportFile discover(SchedulerProperties.Job job, Instant executionStartedAt, String correlationId) {
        return resilientExecutor.execute("file-discovery", job, correlationId, () -> discoverOnce(job, executionStartedAt));
    }

    private ReportFile discoverOnce(SchedulerProperties.Job job, Instant executionStartedAt) throws IOException {
        Path searchPath = searchPath(job);
        if (!Files.isDirectory(searchPath)) {
            throw new CreditSchedulerException("Generated report search path does not exist: " + searchPath);
        }
        String expectedExtension = "." + job.getReport().getFileType().toLowerCase();
        Instant minModifiedTime = executionStartedAt.minusSeconds(1);
        Instant maxModifiedTime = executionStartedAt.plusSeconds(job.getValidation().getMaxSearchWindowSeconds());

        List<Path> candidates;
        try (var stream = Files.list(searchPath)) {
            candidates = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(expectedExtension))
                    .filter(path -> isWithinWindow(path, minModifiedTime, maxModifiedTime))
                    .sorted(Comparator.comparing(this::lastModified).reversed())
                    .toList();
        }

        if (candidates.isEmpty()) {
            throw new CreditSchedulerException("No generated report found in " + searchPath);
        }
        if (job.getValidation().isFailIfMultipleFilesFound() && candidates.size() > 1) {
            throw new CreditSchedulerException("Multiple generated report candidates found in " + searchPath);
        }
        Path latest = candidates.get(0);
        return new ReportFile(
                latest,
                latest.getFileName().toString(),
                Files.size(latest),
                job.getReport().getFileType(),
                lastModified(latest));
    }

    public Path searchPath(SchedulerProperties.Job job) {
        return Path.of(job.getReport().getGeneratedReportBasePath())
                .resolve(job.getReport().getParentFilePath())
                .resolve(job.getReport().getFilePath());
    }

    private boolean isWithinWindow(Path path, Instant minModifiedTime, Instant maxModifiedTime) {
        Instant lastModified = lastModified(path);
        return !lastModified.isBefore(minModifiedTime) && !lastModified.isAfter(maxModifiedTime);
    }

    private Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException exception) {
            throw new CreditSchedulerException("Unable to read file timestamp: " + path, exception);
        }
    }
}
