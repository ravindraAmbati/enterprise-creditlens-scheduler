package com.company.creditscheduler.reports.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportFileValidationServiceTest {

    private final ReportFileValidationService service = new ReportFileValidationService(new ResilientExecutor());

    @TempDir
    Path tempDir;

    @Test
    void acceptsExistingReadableNonEmptyFileWithExpectedType() throws Exception {
        Path file = tempDir.resolve("report.xlsx");
        Files.writeString(file, "1234567890");
        var job = TestFixtures.job();
        job.getValidation().setMinimumFileSizeKb(0);

        assertThatCode(() -> service.validate(job, reportFile(file, 10), "corr"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingFile() {
        Path file = tempDir.resolve("missing.xlsx");

        assertThatThrownBy(() -> service.validate(TestFixtures.job(), reportFile(file, 10), "corr"))
                .hasMessageContaining("Stage failed after retries: file-validation");
    }

    @Test
    void rejectsFileBelowMinimumSize() throws Exception {
        Path file = tempDir.resolve("small.xlsx");
        Files.writeString(file, "tiny");

        assertThatThrownBy(() -> service.validate(TestFixtures.job(), reportFile(file, 4), "corr"))
                .hasMessageContaining("Stage failed after retries: file-validation");
    }

    @Test
    void rejectsUnexpectedFileType() throws Exception {
        Path file = tempDir.resolve("report.pdf");
        Files.writeString(file, "1234567890");
        var job = TestFixtures.job();
        job.getValidation().setMinimumFileSizeKb(0);

        assertThatThrownBy(() -> service.validate(job, reportFile(file, 10), "corr"))
                .hasMessageContaining("Stage failed after retries: file-validation");
    }

    private ReportFile reportFile(Path file, long size) {
        return new ReportFile(file, file.getFileName().toString(), size, "xlsx", Instant.now());
    }
}
