package com.company.creditscheduler.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CorrelationIdsTest {

    @Test
    void createsUniqueCorrelationIds() {
        assertThat(CorrelationIds.newCorrelationId()).isNotEqualTo(CorrelationIds.newCorrelationId());
    }

    @Test
    void executionIdIncludesJobNamePrefix() {
        assertThat(CorrelationIds.newExecutionId("customer-report")).startsWith("customer-report-");
    }
}
