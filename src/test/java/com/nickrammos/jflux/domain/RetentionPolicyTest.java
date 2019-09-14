package com.nickrammos.jflux.domain;

import java.time.Duration;

import org.junit.Test;

public class RetentionPolicyTest {

    private static final String VALID_RP_NAME = "rp";
    private static final Duration VALID_DURATION = Duration.ofDays(1);

    @Test(expected = IllegalStateException.class)
    public void ctor_shouldThrowException_ifNameIsNull() {
        new RetentionPolicy.Builder(null, VALID_DURATION).build();
    }

    @Test(expected = IllegalStateException.class)
    public void ctor_shouldThrowException_ifNameIsEmpty() {
        new RetentionPolicy.Builder("", VALID_DURATION).build();
    }

    @Test(expected = IllegalStateException.class)
    public void ctor_shouldThrowException_ifDurationIsNull() {
        new RetentionPolicy.Builder(VALID_RP_NAME, null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void ctor_shouldThrowException_ifDurationIsNegative() {
        new RetentionPolicy.Builder(VALID_RP_NAME, Duration.ofMillis(-1)).build();
    }

    @Test(expected = IllegalStateException.class)
    public void ctor_shouldThrowException_ifReplicationIsNegative() {
        new RetentionPolicy.Builder(VALID_RP_NAME, VALID_DURATION).replication(-1)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void ctor_shouldThrowException_ifShardDurationIsNegative() {
        new RetentionPolicy.Builder(VALID_RP_NAME, VALID_DURATION).shardDuration(
                Duration.ofDays(-1)).build();
    }
}