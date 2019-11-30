package com.nickrammos.jflux;

import java.util.Collections;
import java.util.List;

import com.nickrammos.jflux.domain.Point;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFluxClientIT extends AbstractJFluxClientIT {

    @Test
    public void testWriteAndReadPoints() {
        // Given
        String measurementName = "some_measurement";

        String tagKey = "some_tag";
        String tagValue = "tag value";
        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap(tagKey, tagValue))
                .build();

        // When
        jFluxClient.writePoint(dbName, measurementName, point);

        // Then
        List<Point> points = jFluxClient.getAllPoints(dbName, measurementName);
        assertThat(points).isNotEmpty();
    }

    @Test
    public void write_shouldReturnEmptyList_ifNoResults() {
        List<Point> points = jFluxClient.getAllPoints(dbName, "non_existent_measurement");
        assertThat(points).isEmpty();
    }
}
