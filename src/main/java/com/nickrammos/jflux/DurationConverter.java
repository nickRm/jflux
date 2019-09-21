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

    /**
     * Converts a duration to a literal which can be used in queries.
     * <p>
     * Currently only hours, minutes, and seconds are supported in the output, meaning that other
     * units are converted to one of the aforementioned. For example, the input
     * {@code Duration.ofDays(1)} is converted to the literal {@code 24h}. Note that, as seen in the
     * previous example, if values for any units are zero then they can be omitted from the
     * generated literal. If all values are zero ({@code Duration.ZERO}) then the output is
     * {@code 0s}.
     *
     * @param duration the duration to convert
     *
     * @return the duration literal
     *
     * @throws NullPointerException     if {@code duration} is {@code null}
     * @throws IllegalArgumentException if {@code duration} is negative
     */
    String toLiteral(Duration duration) {
        if (duration == null) {
            throw new NullPointerException("Duration cannot be null");
        }

        if (duration.isNegative()) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }

        if (duration.isZero()) {
            return "0s";
        }

        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();

        return (hours > 0 ? hours + "h" : "") + (minutes > 0 ? minutes + "m" : "")
                + (seconds > 0 ? seconds + "s" : "");
    }
}
