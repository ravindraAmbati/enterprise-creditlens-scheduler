package com.company.creditscheduler.dto;

import java.nio.file.Path;
import java.time.Instant;

public record ReportFile(
        Path path,
        String actualFileName,
        long sizeBytes,
        String fileType,
        Instant generatedTimestamp
) {
}
