package com.nickrammos.jflux;

import java.io.IOException;
import java.util.Collections;

import com.nickrammos.jflux.domain.Point;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JFluxClientIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";
    private static final String DB_NAME =
            JFluxClientIT.class.getSimpleName() + "_" + System.currentTimeMillis();

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        jFluxClient = new JFluxClient.Builder(INFLUX_DB_URL).build();
        jFluxClient.createDatabase(DB_NAME);
    }

    @After
    public void tearDown() {
        jFluxClient.dropDatabase(DB_NAME);
    }

    @Test
    public void write_shouldWritePoints() {
        // Given
        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        jFluxClient.writePoint(DB_NAME, "some_measurement", point);

        // Then
        // No exception should be thrown.
    }

    @Test(expected = IllegalArgumentException.class)
    public void write_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        jFluxClient.writePoint("non_existent_db", "some_measurement", point);

        // Then
        // Exception should be thrown.
    }

    @Test(expected = IllegalArgumentException.class)
    public void write_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        // Given
        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        jFluxClient.writePoint(DB_NAME, "some_measurement", "non_existent_rp", point);

        // Then
        // Exception should be thrown.
    }
}
