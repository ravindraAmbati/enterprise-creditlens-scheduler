package com.company.creditscheduler.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CredentialsTest {

    @Test
    void exposesPasswordAsStringAndClearsInMemoryChars() {
        Credentials credentials = new Credentials("user", "secret".toCharArray());

        assertThat(credentials.passwordAsString()).isEqualTo("secret");

        credentials.clear();

        assertThat(credentials.password()).containsOnly('\0');
    }
}
