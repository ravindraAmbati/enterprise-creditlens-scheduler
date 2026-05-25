package com.company.creditscheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.ldap")
public class SecurityProperties {

    private boolean enabled;
    private String url;
    private String baseDn;
    private String userDnPattern;
    private String serviceAccountGroup = "SA-SVC-creditlens-scheduler";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getUserDnPattern() {
        return userDnPattern;
    }

    public void setUserDnPattern(String userDnPattern) {
        this.userDnPattern = userDnPattern;
    }

    public String getServiceAccountGroup() {
        return serviceAccountGroup;
    }

    public void setServiceAccountGroup(String serviceAccountGroup) {
        this.serviceAccountGroup = serviceAccountGroup;
    }
}
