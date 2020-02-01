package com.github.nickrm.jflux.domain;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class RetentionPolicyTest {

    private static final String VALID_RP_NAME = "rp";
    private static final Duration VALID_DURATION = Duration.ofDays(1);

    @Test
    public void ctor_shouldThrowException_ifNameIsNull() {
        assertThatIllegalStateException().isThrownBy(
                () -> new RetentionPolicy.Builder(null, VALID_DURATION).build());
    }

    @Test
    public void ctor_shouldThrowException_ifNameIsEmpty() {
        assertThatIllegalStateException().isThrownBy(
                () -> new RetentionPolicy.Builder("", VALID_DURATION).build());
    }

    @Test
    public void ctor_shouldThrowException_ifDurationIsNull() {
        assertThatIllegalStateException().isThrownBy(
                () -> new RetentionPolicy.Builder(VALID_RP_NAME, null).build());
    }

    @Test
    public void ctor_shouldThrowException_ifDurationIsNegative() {
        assertThatIllegalStateException().isThrownBy(
                () -> new RetentionPolicy.Builder(VALID_RP_NAME, Duration.ofMillis(-1)).build());
    }

    @Test
    public void ctor_shouldThrowException_ifReplicationIsNegative() {
        assertThatIllegalStateException().isThrownBy(
                () -> new RetentionPolicy.Builder(VALID_RP_NAME, VALID_DURATION).replication(-1)
                        .build());
    }

    @Test
    public void ctor_shouldThrowException_ifShardDurationIsNegative() {
        assertThatIllegalStateException().isThrownBy(
                () -> new RetentionPolicy.Builder(VALID_RP_NAME, VALID_DURATION).shardDuration(
                        Duration.ofDays(-1)).build());
    }

    @Test
    public void withDuration_shouldThrowException_ifNewValueIsNull() {
        assertThatIllegalStateException().isThrownBy(
                () -> createValidInstance().withDuration(null));
    }

    @Test
    public void withDuration_shouldThrowException_ifNewValueIsNegative() {
        assertThatIllegalStateException().isThrownBy(
                () -> createValidInstance().withDuration(Duration.ofMillis(-1)));
    }

    @Test
    public void withReplication_shouldThrowException_ifNewValueIsNegative() {
        assertThatIllegalStateException().isThrownBy(
                () -> createValidInstance().withReplication(-1));
    }

    @Test
    public void withShardDuration_shouldThrowException_ifNewValueIsNull() {
        assertThatIllegalStateException().isThrownBy(
                () -> createValidInstance().withShardDuration(null));
    }

    @Test
    public void withShardDuration_shouldThrowException_ifNewValueIsNegative() {
        assertThatIllegalStateException().isThrownBy(
                () -> createValidInstance().withShardDuration(Duration.ofMillis(-1)));
    }

    private static RetentionPolicy createValidInstance() {
        return new RetentionPolicy.Builder("some_name", Duration.ZERO).build();
    }
}