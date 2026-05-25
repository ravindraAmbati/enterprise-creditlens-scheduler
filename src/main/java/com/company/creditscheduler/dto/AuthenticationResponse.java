package com.company.creditscheduler.dto;

public record AuthenticationResponse(Payload payLoad, Object status) {

    public String token() {
        return payLoad == null ? null : payLoad.token();
    }

    public record Payload(Object user, String token, boolean devMode) {
    }
}
