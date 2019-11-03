package com.nickrammos.jflux;

import java.time.Duration;

import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.RetentionPolicy;

/**
 * Handles conversions between {@link RetentionPolicy} and {@link Point}.
 */
final class RetentionPolicyConverter {

    private final DurationConverter durationConverter;

    /**
     * Initializes a new instance.
     */
    RetentionPolicyConverter() {
        this.durationConverter = new DurationConverter();
    }

    /**
     * Creates a retention policy by parsing the fields in the specified point.
     *
     * @param point the point to parse, not {@code null}
     *
     * @return the retention policy
     *
     * @throws IllegalArgumentException if {@code point} is {@code null}
     */
    RetentionPolicy parsePoint(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }

        String retentionPolicyName = point.getTags().get("name");
        Duration duration = durationConverter.parseDuration(point.getTags().get("duration"));
        int replication = Integer.parseInt(String.valueOf(point.getFields().get("replicaN")));
        Duration shardDuration =
                durationConverter.parseDuration(point.getTags().get("shardGroupDuration"));
        boolean isDefault =
                Boolean.parseBoolean(String.valueOf(point.getFields().get("default")));

        return new RetentionPolicy.Builder(retentionPolicyName, duration).replication(replication)
                .shardDuration(shardDuration)
                .isDefault(isDefault)
                .build();
    }
}
