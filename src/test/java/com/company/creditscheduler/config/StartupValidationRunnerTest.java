package com.company.creditscheduler.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.creditscheduler.TestFixtures;
import com.company.creditscheduler.authentication.CredentialProvider;
import com.company.creditscheduler.dto.Credentials;
import com.company.creditscheduler.exception.CreditSchedulerException;
import com.company.creditscheduler.notifications.retry.ResilientExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class StartupValidationRunnerTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private CredentialProvider credentialProvider;
    @Mock
    private ResilientExecutor resilientExecutor;

    private SecurityProperties securityProperties;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        Mockito.lenient().doAnswer(invocation -> {
            ResilientExecutor.ThrowingRunnable runnable = invocation.getArgument(3);
            runnable.run();
            return null;
        }).when(resilientExecutor).executeVoid(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void validatesEnabledJobAndClearsCredentials() {
        var job = TestFixtures.job();
        var credentials = new Credentials("user", "secret".toCharArray());
        when(credentialProvider.getCredentials("CreditLens/customer-report")).thenReturn(credentials);
        var runner = runner(TestFixtures.schedulerProperties(job));

        assertThatCode(runner::run).doesNotThrowAnyException();
        verify(credentialProvider).getCredentials("CreditLens/customer-report");
        org.assertj.core.api.Assertions.assertThat(credentials.password()).containsOnly('\0');
    }

    @Test
    void ignoresDisabledJobsDuringCredentialValidation() {
        var job = TestFixtures.job();
        job.setEnabled(false);
        var runner = runner(TestFixtures.schedulerProperties(job));

        assertThatCode(runner::run).doesNotThrowAnyException();
        org.mockito.Mockito.verifyNoInteractions(credentialProvider);
    }

    @Test
    void failsForInvalidCronExpression() {
        var job = TestFixtures.job();
        job.setCron("bad cron");
        var runner = runner(TestFixtures.schedulerProperties(job));

        assertThatThrownBy(runner::run)
                .isInstanceOf(CreditSchedulerException.class)
                .hasMessageContaining("Invalid cron expression");
    }

    @Test
    void failsForInvalidEndpoint() {
        var job = TestFixtures.job();
        job.getAuthentication().setEndpoint("not-a-url");
        var runner = runner(TestFixtures.schedulerProperties(job));

        assertThatThrownBy(runner::run)
                .isInstanceOf(CreditSchedulerException.class)
                .hasMessageContaining("Invalid authentication endpoint");
    }

    @Test
    void failsForInvalidAdConfigurationWhenAdIsEnabled() {
        var job = TestFixtures.job();
        when(credentialProvider.getCredentials("CreditLens/customer-report"))
                .thenReturn(new Credentials("user", "secret".toCharArray()));
        securityProperties.setEnabled(true);
        var runner = runner(TestFixtures.schedulerProperties(job));

        assertThatThrownBy(runner::run)
                .isInstanceOf(CreditSchedulerException.class)
                .hasMessageContaining("Invalid AD configuration");
    }

    @Test
    void sendsStartupTestEmailWhenEnabled() {
        var job = TestFixtures.job();
        when(credentialProvider.getCredentials("CreditLens/customer-report"))
                .thenReturn(new Credentials("user", "secret".toCharArray()));
        var properties = TestFixtures.schedulerProperties(job);
        properties.getStartupValidation().setSmtpTestEmailEnabled(true);
        properties.getStartupValidation().setSmtpTestRecipient("it@example.com");
        var runner = runner(properties);

        assertThatCode(runner::run).doesNotThrowAnyException();
        verify(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    private StartupValidationRunner runner(SchedulerProperties schedulerProperties) {
        return new StartupValidationRunner(
                schedulerProperties,
                securityProperties,
                mailSender,
                credentialProvider,
                resilientExecutor);
    }
}
