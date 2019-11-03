package com.nickrammos.jflux;

import java.io.IOException;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Tag;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JFluxClientAnnotationIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";
    private static final String DB_NAME =
            JFluxClientAnnotationIT.class.getSimpleName() + "_" + System.currentTimeMillis();

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
    public void write_shouldWriteAnnotatedClass() {
        // Given
        TestMeasurement testPoint = new TestMeasurement();
        testPoint.testField = 4;
        testPoint.otherField = -1;
        testPoint.tag = "some tag value";

        // When
        jFluxClient.write(DB_NAME, testPoint);

        // Then
        // No exception should be thrown.
    }

    private static class TestMeasurement {

        @Field
        private int testField;

        private int otherField;

        @Tag("test_tag")
        private String tag;
    }
}
