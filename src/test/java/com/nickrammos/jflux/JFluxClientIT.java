package com.nickrammos.jflux;

import java.util.Collections;

import com.nickrammos.jflux.domain.Point;

import org.junit.Test;

public class JFluxClientIT extends AbstractJFluxClientIT {

    @Test
    public void write_shouldWritePoints() {
        // Given
        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        jFluxClient.writePoint(dbName, "some_measurement", point);

        // Then
        // No exception should be thrown.
    }
}
