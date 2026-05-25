package com.company.creditscheduler.util;

import java.util.UUID;

public final class CorrelationIds {

    private CorrelationIds() {
    }

    public static String newCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static String newExecutionId(String jobName) {
        return jobName + "-" + UUID.randomUUID();
    }
}
