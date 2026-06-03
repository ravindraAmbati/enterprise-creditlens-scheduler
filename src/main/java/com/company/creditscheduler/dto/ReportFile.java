package com.company.creditscheduler.dto;

import java.nio.file.Path;
import java.time.Instant;

public final class ReportFile {

    private final Path path;
    private final String actualFileName;
    private final long sizeBytes;
    private final String fileType;
    private final Instant generatedTimestamp;

    public ReportFile(Path path, String actualFileName, long sizeBytes, String fileType, Instant generatedTimestamp) {
        this.path = path;
        this.actualFileName = actualFileName;
        this.sizeBytes = sizeBytes;
        this.fileType = fileType;
        this.generatedTimestamp = generatedTimestamp;
    }

    public Path path() {
        return path;
    }

    public Path getPath() {
        return path;
    }

    public String actualFileName() {
        return actualFileName;
    }

    public String getActualFileName() {
        return actualFileName;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String fileType() {
        return fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public Instant generatedTimestamp() {
        return generatedTimestamp;
    }

    public Instant getGeneratedTimestamp() {
        return generatedTimestamp;
    }
}
