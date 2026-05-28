package com.company.creditscheduler.notifications.email;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.ReportFile;
import java.nio.file.Path;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class EmailContentRenderer {

    public String businessSuccess(
            SchedulerProperties.Job job,
            ReportFile reportFile,
            String executionId,
            String correlationId,
            String footer
    ) {
        return page("CreditLens Report Generated",
                row("Report Name", job.getName())
                        + row("Actual Generated File Name", reportFile.actualFileName())
                        + row("File Size", reportFile.sizeBytes() + " bytes")
                        + row("File Type", reportFile.fileType())
                        + row("File Path", reportFile.path())
                        + row("Download Details", "Available from configured CreditLens repository path")
                        + row("Generated Timestamp", reportFile.generatedTimestamp())
                        + row("Execution ID", executionId)
                        + row("Correlation ID", correlationId),
                footer);
    }

    public String itFailure(
            SchedulerProperties.Job job,
            String failedStage,
            String errorDetails,
            int retryCount,
            String executionId,
            String correlationId,
            Instant failureTimestamp,
            String serverName,
            Path searchPath,
            int validationWindowSeconds,
            String footer
    ) {
        return page("CreditLens Scheduler Failure",
                row("Job Name", job.getName())
                        + row("Error Details", errorDetails)
                        + row("Retry Count", retryCount)
                        + row("Execution ID", executionId)
                        + row("Correlation ID", correlationId)
                        + row("Failure Timestamp", failureTimestamp)
                        + row("Server Name", serverName)
                        + row("Search Path", searchPath)
                        + row("Validation Window", validationWindowSeconds + " seconds")
                        + row("Failed Stage", failedStage),
                footer);
    }

    private String page(String title, String rows, String footer) {
        return """
                <!doctype html>
                <html lang="en">
                <body>
                <h2>%s</h2>
                <table>
                %s
                </table>
                <p>%s</p>
                </body>
                </html>
                """.formatted(escape(title), rows, escape(footer));
    }

    private String row(String label, Object value) {
        return "<tr><th align=\"left\">"
                + escape(label)
                + "</th><td>"
                + escape(String.valueOf(value))
                + "</td></tr>";
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
