package com.company.creditscheduler.notifications.email;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.dto.ReportFile;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class EmailContentRendererTest {

    private final EmailContentRenderer renderer = new EmailContentRenderer();

    @Test
    void rendersBusinessSuccessEmailWithEscapedDynamicValues() {
        var job = TestFixtures.job();
        job.setName("customer-report<script>");
        var reportFile = new ReportFile(
                Path.of("C:/reports/customer.xlsx"),
                "customer.xlsx",
                2048,
                "xlsx",
                Instant.parse("2026-05-27T10:15:30Z"));

        String html = renderer.businessSuccess(job, reportFile, "exec-1", "corr-1", "footer");

        assertThat(html).contains("CreditLens Report Generated");
        assertThat(html).contains("customer-report&lt;script&gt;");
        assertThat(html).contains("customer.xlsx");
        assertThat(html).contains("2048 bytes");
        assertThat(html).contains("corr-1");
    }

    @Test
    void rendersItFailureEmailWithRequiredOperationalFields() {
        var job = TestFixtures.job();

        String html = renderer.itFailure(
                job,
                "file-discovery",
                "No file found",
                5,
                "exec-1",
                "corr-1",
                Instant.parse("2026-05-27T10:15:30Z"),
                "server-1",
                Path.of("D:/reports/customer"),
                120,
                "footer");

        assertThat(html).contains("CreditLens Scheduler Failure");
        assertThat(html).contains("No file found");
        assertThat(html).contains("file-discovery");
        assertThat(html).contains("server-1");
        assertThat(html).contains("120 seconds");
    }
}
