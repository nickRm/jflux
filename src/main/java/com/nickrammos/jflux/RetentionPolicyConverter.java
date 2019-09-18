package com.nickrammos.jflux;

import java.time.Duration;

import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.RetentionPolicy;

/**
 * Handles conversions between {@link RetentionPolicy} and {@link Point}.
 */
final class RetentionPolicyConverter {

    /**
     * Creates a retention policy by parsing the fields in the specified point.
     *
     * @param point the point to parse, not {@code null}
     *
     * @return the retention policy
     *
     * @throws NullPointerException if {@code point} is {@code null}
     */
    RetentionPolicy parsePoint(Point point) {
        if (point == null) {
            throw new NullPointerException("Point cannot be null");
        }

        String retentionPolicyName = point.getTags().get("name");
        Duration duration = parseDuration(point.getTags().get("duration"));
        int replication = Integer.parseInt(String.valueOf(point.getFields().get("replicaN")));
        Duration shardDuration = parseDuration(point.getTags().get("shardGroupDuration"));
        boolean isDefault =
                Boolean.parseBoolean(String.valueOf(point.getFields().get("default")));

        return new RetentionPolicy.Builder(retentionPolicyName, duration).replication(replication)
                .shardDuration(shardDuration)
                .isDefault(isDefault)
                .build();
    }

    private Duration parseDuration(String textValue) {
        // The text value is in the format 1h2m3s, where the h and m fields can be missing if zero.
        String[] values = textValue.split("[hms]");
        long hours = values.length == 3 ? Long.parseLong(values[values.length - 3]) : 0;
        long minutes = values.length >= 2 ? Long.parseLong(values[values.length - 2]) : 0;
        long seconds = Long.parseLong(values[values.length - 1]);
        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
