package com.nickrammos.jflux;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.nickrammos.jflux.domain.Point;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class LineProtocolConverterTest {

    private final LineProtocolConverter lineProtocolConverter = new LineProtocolConverter();

    @Test
    public void toLineProtocol_shouldConstructLineProtocol_forSinglePoint() {
        // Given
        String measurementName = "some_measurement";
        Instant timestamp = Instant.now();
        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "some value"))
                .timestamp(timestamp)
                .build();

        // When
        List<String> lineProtocols =
                lineProtocolConverter.toLineProtocol(measurementName, Collections.singleton(point));

        // Then
        assertThat(lineProtocols).containsExactly(
                "some_measurement,some_tag=some\\ value some_field=1 " + timestamp.toEpochMilli());
    }

    @Test
    public void toLineProtocol_shouldConstructSingleLine_forPointsInSameSeries() {
        // Given
        String measurementName = "some_measurement";
        String fieldName = "some_field";
        Map<String, String> tags = Collections.singletonMap("some_tag", "tag1");

        Point point1 = new Point.Builder().fields(Collections.singletonMap(fieldName, 1))
                .tags(tags)
                .build();
        Point point2 = new Point.Builder().fields(Collections.singletonMap(fieldName, 2))
                .tags(tags)
                .build();

        // When
        List<String> lineProtocols = lineProtocolConverter.toLineProtocol(measurementName,
                Arrays.asList(point1, point2));

        // Then
        assertThat(lineProtocols.size()).isEqualTo(1);
    }

    @Test
    public void toLineProtocol_shouldConstructSeparateLines_forPointsInDifferentSeries() {
        // Given
        String measurementName = "some_measurement";
        String fieldName = "some_field";
        String tagName = "some_tag";

        Point point1 = new Point.Builder().fields(Collections.singletonMap(fieldName, 1))
                .tags(Collections.singletonMap(tagName, "1"))
                .build();
        Point point2 = new Point.Builder().fields(Collections.singletonMap(fieldName, 2))
                .tags(Collections.singletonMap(tagName, "2"))
                .build();

        // When
        List<String> lineProtocols = lineProtocolConverter.toLineProtocol(measurementName,
                Arrays.asList(point1, point2));

        // Then
        assertThat(lineProtocols.size()).isEqualTo(2);
    }

    @Test
    public void toLineProtocol_shouldThrowException_ifMeasurementNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> lineProtocolConverter.toLineProtocol(null, Collections.emptyList()));
    }

    @Test
    public void toLineProtocol_shouldThrowException_ifMeasurementNameIsEmpty() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> lineProtocolConverter.toLineProtocol("", Collections.emptyList()));
    }
}