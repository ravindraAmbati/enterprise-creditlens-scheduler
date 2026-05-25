package com.company.creditscheduler.authentication;

import com.company.creditscheduler.dto.Credentials;

public interface CredentialProvider {

    Credentials getCredentials(String targetName);
}
