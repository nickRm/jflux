package com.nickrammos.jflux;

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;

/**
 * Handles conversions between instances of {@link Duration} and InfluxDB duration literals.
 *
 * @see <a href="https://docs.influxdata.com/influxdb/v1.7/query_language/spec/#literals">InfluxDB
 * literals</a>
 */
final class DurationConverter {

    /**
     * Parses a duration literal into an instance of {@link Duration}.
     * <p>
     * The currently supported format for literals is {@code 1h2m3s}, where the hours and minutes
     * can be omitted if their value is zero.
     *
     * @param durationLiteral the literal to parse
     *
     * @return new duration instance
     *
     * @throws IllegalArgumentException if the literal is {@code null} or empty
     */
    Duration parseDuration(String durationLiteral) {
        if (StringUtils.isBlank(durationLiteral)) {
            throw new IllegalArgumentException("Duration literal cannot be blank");
        }

        String[] values = durationLiteral.split("[hms]");
        long hours = values.length == 3 ? Long.parseLong(values[values.length - 3]) : 0;
        long minutes = values.length >= 2 ? Long.parseLong(values[values.length - 2]) : 0;
        long seconds = Long.parseLong(values[values.length - 1]);

        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
