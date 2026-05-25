package com.company.creditscheduler.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthenticationResponseTest {

    @Test
    void returnsNestedTokenWhenPayloadExists() {
        var response = new AuthenticationResponse(new AuthenticationResponse.Payload(null, "bearer", false), null);

        assertThat(response.token()).isEqualTo("bearer");
    }

    @Test
    void returnsNullTokenWhenPayloadIsMissing() {
        var response = new AuthenticationResponse(null, null);

        assertThat(response.token()).isNull();
    }
}
