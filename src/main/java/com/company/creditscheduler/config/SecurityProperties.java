package com.company.creditscheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private boolean serviceAccountAuthenticationEnabled = false;
    private Ldap ldap = new Ldap();

    public boolean isServiceAccountAuthenticationEnabled() {
        return serviceAccountAuthenticationEnabled;
    }

    public void setServiceAccountAuthenticationEnabled(boolean serviceAccountAuthenticationEnabled) {
        this.serviceAccountAuthenticationEnabled = serviceAccountAuthenticationEnabled;
    }

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(Ldap ldap) {
        this.ldap = ldap;
    }

    public boolean isEnabled() {
        return ldap.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.ldap.enabled = enabled;
    }

    public String getUrl() {
        return ldap.url;
    }

    public void setUrl(String url) {
        this.ldap.url = url;
    }

    public String getBaseDn() {
        return ldap.baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.ldap.baseDn = baseDn;
    }

    public String getUserDnPattern() {
        return ldap.userDnPattern;
    }

    public void setUserDnPattern(String userDnPattern) {
        this.ldap.userDnPattern = userDnPattern;
    }

    public String getServiceAccountGroup() {
        return ldap.serviceAccountGroup;
    }

    public void setServiceAccountGroup(String serviceAccountGroup) {
        this.ldap.serviceAccountGroup = serviceAccountGroup;
    }

    public static class Ldap {
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
}
