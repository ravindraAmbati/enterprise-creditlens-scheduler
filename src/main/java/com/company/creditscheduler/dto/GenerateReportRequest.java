package com.company.creditscheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public final class GenerateReportRequest {

    private final ReportInput reportInput;
    private final Map<String, Object> contextInfo;

    public GenerateReportRequest(ReportInput reportInput, Map<String, Object> contextInfo) {
        this.reportInput = reportInput;
        this.contextInfo = contextInfo;
    }

    public ReportInput reportInput() {
        return reportInput;
    }

    public ReportInput getReportInput() {
        return reportInput;
    }

    public Map<String, Object> contextInfo() {
        return contextInfo;
    }

    public Map<String, Object> getContextInfo() {
        return contextInfo;
    }

    public static final class ReportInput {
        private final String reportId;
        private final boolean isPortfolio;
        private final List<Object> reportParams;

        public ReportInput(String reportId, boolean isPortfolio, List<Object> reportParams) {
            this.reportId = reportId;
            this.isPortfolio = isPortfolio;
            this.reportParams = reportParams;
        }

        public String reportId() {
            return reportId;
        }

        public String getReportId() {
            return reportId;
        }

        @JsonProperty("isPortfolio")
        public boolean isPortfolio() {
            return isPortfolio;
        }

        public List<Object> reportParams() {
            return reportParams;
        }

        public List<Object> getReportParams() {
            return reportParams;
        }
    }
}
