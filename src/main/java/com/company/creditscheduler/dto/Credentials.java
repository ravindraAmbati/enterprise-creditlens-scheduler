package com.company.creditscheduler.dto;

import java.util.Arrays;

public final class Credentials {

    private final String username;
    private final char[] password;

    public Credentials(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    public String username() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public char[] password() {
        return password;
    }

    public char[] getPassword() {
        return password;
    }

    public String passwordAsString() {
        return new String(password);
    }

    public void clear() {
        Arrays.fill(password, '\0');
    }
}
