package com.company.creditscheduler.reports.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportFileDiscoveryServiceTest {

    private final ReportFileDiscoveryService service = new ReportFileDiscoveryService(new ResilientExecutor());

    @TempDir
    Path tempDir;

    @Test
    void discoversLatestMatchingFileInsideExecutionWindow() throws Exception {
        var job = TestFixtures.job();
        configureSearchBase(job);
        Path searchPath = Files.createDirectories(service.searchPath(job));
        Instant started = Instant.now();
        Path older = write(searchPath.resolve("older.xlsx"), "older", started.plusSeconds(5));
        Path latest = write(searchPath.resolve("latest.xlsx"), "latest", started.plusSeconds(10));
        write(searchPath.resolve("ignored.pdf"), "ignored", started.plusSeconds(20));
        job.getValidation().setFailIfMultipleFilesFound(false);

        var result = service.discover(job, started, "corr");

        assertThat(result.path()).isEqualTo(latest);
        assertThat(result.actualFileName()).isEqualTo("latest.xlsx");
        assertThat(result.sizeBytes()).isEqualTo(Files.size(latest));
        assertThat(older).exists();
    }

    @Test
    void failsWhenSearchPathDoesNotExist() {
        var job = TestFixtures.job();
        configureSearchBase(job);

        assertThatThrownBy(() -> service.discover(job, Instant.now(), "corr"))
                .hasMessageContaining("Stage failed after retries: file-discovery");
    }

    @Test
    void failsWhenNoCandidateMatchesFileTypeAndWindow() throws Exception {
        var job = TestFixtures.job();
        configureSearchBase(job);
        Path searchPath = Files.createDirectories(service.searchPath(job));
        Instant started = Instant.now();
        write(searchPath.resolve("wrong.pdf"), "content", started.plusSeconds(1));

        assertThatThrownBy(() -> service.discover(job, started, "corr"))
                .hasMessageContaining("Stage failed after retries: file-discovery");
    }

    @Test
    void failsWhenMultipleCandidatesFoundAndPolicyRequiresUniqueness() throws Exception {
        var job = TestFixtures.job();
        configureSearchBase(job);
        Path searchPath = Files.createDirectories(service.searchPath(job));
        Instant started = Instant.now();
        write(searchPath.resolve("one.xlsx"), "one", started.plusSeconds(1));
        write(searchPath.resolve("two.xlsx"), "two", started.plusSeconds(2));

        assertThatThrownBy(() -> service.discover(job, started, "corr"))
                .hasMessageContaining("Stage failed after retries: file-discovery");
    }

    @Test
    void buildsSearchPathFromConfiguredBaseParentAndFilePath() {
        var job = TestFixtures.job();
        configureSearchBase(job);

        assertThat(service.searchPath(job)).isEqualTo(tempDir.resolve("reports").resolve("customer"));
    }

    private void configureSearchBase(com.company.creditscheduler.config.SchedulerProperties.Job job) {
        job.getReport().setGeneratedReportBasePath(tempDir.toString());
    }

    private Path write(Path path, String content, Instant modified) throws Exception {
        Files.writeString(path, content);
        Files.setLastModifiedTime(path, FileTime.from(modified));
        return path;
    }
}
