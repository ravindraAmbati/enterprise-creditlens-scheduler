package com.company.creditscheduler.dto;

public record Credentials(String username, char[] password) {

    public String passwordAsString() {
        return new String(password);
    }

    public void clear() {
        java.util.Arrays.fill(password, '\0');
    }
}
