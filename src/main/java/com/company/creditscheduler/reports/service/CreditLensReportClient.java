package com.company.creditscheduler.reports.service;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.GenerateReportRequest;
import com.company.creditscheduler.governance.ExternalCommunicationLogger;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CreditLensReportClient {

    private final WebClient webClient;
    private final ExternalCommunicationLogger communicationLogger;
    private final ResilientExecutor resilientExecutor;

    public CreditLensReportClient(
            WebClient.Builder webClientBuilder,
            ExternalCommunicationLogger communicationLogger,
            ResilientExecutor resilientExecutor
    ) {
        this.webClient = webClientBuilder.build();
        this.communicationLogger = communicationLogger;
        this.resilientExecutor = resilientExecutor;
    }

    public void generate(SchedulerProperties.Job job, String token, String correlationId) {
        communicationLogger.log(job.getReport().getEndpoint(), job.getName(), correlationId);
        resilientExecutor.executeVoid("report-generation-api", job, correlationId, () -> {
            GenerateReportRequest request = new GenerateReportRequest(
                    new GenerateReportRequest.ReportInput(
                            job.getReportInput().getReportId(),
                            job.getReportInput().isPortfolio(),
                            job.getReportInput().getReportParams()),
                    Map.of());

            webClient.post()
                    .uri(job.getReport().getEndpoint() + "/{filePath}/{fileName}/{fileType}",
                            job.getReport().getFilePath(),
                            job.getReport().getFileName(),
                            job.getReport().getFileType())
                    .headers(headers -> headers.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.ACCEPTED,
                            response -> response.bodyToMono(String.class).map(RuntimeException::new))
                    .toBodilessEntity()
                    .block();
        });
    }
}
