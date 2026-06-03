package com.company.creditscheduler.dto;

public class AuthenticationResponse {

    private Payload payLoad;
    private Object status;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(Payload payLoad, Object status) {
        this.payLoad = payLoad;
        this.status = status;
    }

    public Payload payLoad() {
        return payLoad;
    }

    public Payload getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(Payload payLoad) {
        this.payLoad = payLoad;
    }

    public Object status() {
        return status;
    }

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public String token() {
        return payLoad == null ? null : payLoad.token();
    }

    public static class Payload {
        private Object user;
        private String token;
        private boolean devMode;

        public Payload() {
        }

        public Payload(Object user, String token, boolean devMode) {
            this.user = user;
            this.token = token;
            this.devMode = devMode;
        }

        public Object user() {
            return user;
        }

        public Object getUser() {
            return user;
        }

        public void setUser(Object user) {
            this.user = user;
        }

        public String token() {
            return token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean devMode() {
            return devMode;
        }

        public boolean isDevMode() {
            return devMode;
        }

        public void setDevMode(boolean devMode) {
            this.devMode = devMode;
        }
    }
}
