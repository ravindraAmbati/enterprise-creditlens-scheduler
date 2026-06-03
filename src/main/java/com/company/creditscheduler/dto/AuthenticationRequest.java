package com.company.creditscheduler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class AuthenticationRequest {

    private final String userName;
    private final String password;

    public AuthenticationRequest(
            @JsonProperty("UserName") String userName,
            @JsonProperty("Password") String password
    ) {
        this.userName = userName;
        this.password = password;
    }

    @JsonProperty("UserName")
    public String userName() {
        return userName;
    }

    @JsonProperty("UserName")
    public String getUserName() {
        return userName;
    }

    @JsonProperty("Password")
    public String password() {
        return password;
    }

    @JsonProperty("Password")
    public String getPassword() {
        return password;
    }
}
