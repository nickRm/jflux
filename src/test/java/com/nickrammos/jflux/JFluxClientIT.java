package com.nickrammos.jflux;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Tag;
import com.nickrammos.jflux.annotation.Timestamp;
import com.nickrammos.jflux.domain.Point;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testWriteAndReadAnnotatedClass() {
        // Given
        TestAnnotatedClass instanceToWrite = new TestAnnotatedClass();
        instanceToWrite.timestamp = Instant.now();
        instanceToWrite.aField = 5;
        instanceToWrite.aTag = "tag value";

        // When
        jFluxClient.write(dbName, instanceToWrite);

        // Then
        List<TestAnnotatedClass> results =
                jFluxClient.getAllPoints(dbName, TestAnnotatedClass.class);
        assertThat(results).containsExactly(instanceToWrite);
    }

    private static class TestAnnotatedClass {

        @Timestamp
        private Instant timestamp;

        @Field
        private Integer aField;

        @Tag
        private String aTag;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestAnnotatedClass that = (TestAnnotatedClass) o;
            return Objects.equals(timestamp, that.timestamp) &&
                    Objects.equals(aField, that.aField) &&
                    Objects.equals(aTag, that.aTag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, aField, aTag);
        }
    }
}
