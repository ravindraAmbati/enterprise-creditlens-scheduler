package com.company.creditscheduler.authentication;

import com.company.creditscheduler.config.SchedulerProperties;
import com.company.creditscheduler.dto.AuthenticationRequest;
import com.company.creditscheduler.dto.AuthenticationResponse;
import com.company.creditscheduler.dto.Credentials;
import com.company.creditscheduler.exception.CreditSchedulerException;
import com.company.creditscheduler.governance.ExternalCommunicationLogger;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CreditLensAuthenticationClient {

    private final WebClient webClient;
    private final CredentialProvider credentialProvider;
    private final ExternalCommunicationLogger communicationLogger;
    private final ResilientExecutor resilientExecutor;

    public CreditLensAuthenticationClient(
            WebClient.Builder webClientBuilder,
            CredentialProvider credentialProvider,
            ExternalCommunicationLogger communicationLogger,
            ResilientExecutor resilientExecutor
    ) {
        this.webClient = webClientBuilder.build();
        this.credentialProvider = credentialProvider;
        this.communicationLogger = communicationLogger;
        this.resilientExecutor = resilientExecutor;
    }

    public String authenticate(SchedulerProperties.Job job, String correlationId) {
        Credentials credentials = resilientExecutor.execute("credential-retrieval", job, correlationId,
                () -> credentialProvider.getCredentials(job.getCredentialTarget()));
        try {
            communicationLogger.log(job.getAuthentication().getEndpoint(), job.getName(), correlationId);
            AuthenticationResponse response = resilientExecutor.execute("authentication-api", job, correlationId,
                    () -> webClient.post()
                            .uri(job.getAuthentication().getEndpoint())
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new AuthenticationRequest(credentials.username(), credentials.passwordAsString()))
                            .retrieve()
                            .bodyToMono(AuthenticationResponse.class)
                            .block());
            String token = resilientExecutor.execute("token-processing", job, correlationId, () -> {
                if (response == null || response.token() == null || response.token().isBlank()) {
                    throw new CreditSchedulerException("CreditLens authentication response did not include a bearer token");
                }
                return response.token();
            });
            return token;
        } finally {
            credentials.clear();
        }
    }
}
