package com.nickrammos.jflux;

import java.time.Duration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DurationConverterTest {

    private final DurationConverter durationConverter = new DurationConverter();

    @Test
    public void parseDuration_shouldHandleSecondsOnly() {
        Duration expected = Duration.ofSeconds(15);
        Duration actual = durationConverter.parseDuration("15s");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void parseDuration_shouldHandleSecondsAndMinutes() {
        Duration expected = Duration.ofMinutes(20).plusSeconds(15);
        Duration actual = durationConverter.parseDuration("20m15s");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void parseDuration_shouldHandleSecondsMinutesAndHours() {
        Duration expected = Duration.ofHours(6).plusMinutes(20).plusSeconds(15);
        Duration actual = durationConverter.parseDuration("6h20m15s");
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseDuration_shouldThrowException_ifLiteralIsNull() {
        durationConverter.parseDuration(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseDuration_shouldThrowException_ifLiteralIsEmpty() {
        durationConverter.parseDuration("");
    }

    @Test
    public void toLiteral_shouldConvertDuration() {
        Duration duration = Duration.ofHours(1).plusMinutes(20).plusSeconds(15);
        String literal = durationConverter.toLiteral(duration);
        assertThat(literal).isEqualTo("1h20m15s");
    }

    @Test
    public void toLiteral_shouldOmit_zeroValues() {
        Duration duration = Duration.ofMinutes(10);
        String literal = durationConverter.toLiteral(duration);
        assertThat(literal).isEqualTo("10m");
    }

    @Test
    public void toLiteral_shouldConvertZeroDuration() {
        Duration duration = Duration.ZERO;
        String literal = durationConverter.toLiteral(duration);
        assertThat(literal).isEqualTo("0s");
    }

    @Test(expected = NullPointerException.class)
    public void toLiteral_shouldThrowException_ifDurationIsNull() {
        durationConverter.toLiteral(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toLiteral_shouldThrowException_ifDurationIsNegative() {
        durationConverter.toLiteral(Duration.ofMillis(-1));
    }
}