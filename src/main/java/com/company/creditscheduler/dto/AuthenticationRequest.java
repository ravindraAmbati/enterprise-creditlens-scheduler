package com.company.creditscheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationRequest(
        @JsonProperty("UserName") String userName,
        @JsonProperty("Password") String password
) {
}
