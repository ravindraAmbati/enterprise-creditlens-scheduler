package com.company.creditscheduler.dto;

import java.util.List;
import java.util.Map;

public record GenerateReportRequest(ReportInput reportInput, Map<String, Object> contextInfo) {

    public record ReportInput(String reportId, boolean isPortfolio, List<Object> reportParams) {
    }
}
