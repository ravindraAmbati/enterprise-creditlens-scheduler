package com.company.creditscheduler.reports.validation;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.ReportFile;
import com.company.creditscheduler.exception.CreditSchedulerException;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import java.nio.file.Files;
import org.springframework.stereotype.Service;

@Service
public class ReportFileValidationService {

    private final ResilientExecutor resilientExecutor;

    public ReportFileValidationService(ResilientExecutor resilientExecutor) {
        this.resilientExecutor = resilientExecutor;
    }

    public void validate(SchedulerProperties.Job job, ReportFile reportFile, String correlationId) {
        resilientExecutor.executeVoid("file-validation", job, correlationId, () -> {
            if (!Files.exists(reportFile.path())) {
                throw new CreditSchedulerException("Generated report file does not exist: " + reportFile.path());
            }
            if (!Files.isReadable(reportFile.path())) {
                throw new CreditSchedulerException("Generated report file is not readable: " + reportFile.path());
            }
            long minimumBytes = job.getValidation().getMinimumFileSizeKb() * 1024;
            if (reportFile.sizeBytes() <= 0 || reportFile.sizeBytes() < minimumBytes) {
                throw new CreditSchedulerException("Generated report file is empty or below minimum size: " + reportFile.path());
            }
            String fileName = reportFile.actualFileName().toLowerCase();
            if (!fileName.endsWith("." + job.getReport().getFileType().toLowerCase())) {
                throw new CreditSchedulerException("Generated report file type mismatch: " + reportFile.path());
            }
        });
    }
}
