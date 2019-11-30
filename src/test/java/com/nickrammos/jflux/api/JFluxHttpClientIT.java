package com.nickrammos.jflux.api;

import java.io.IOException;

import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.exception.InfluxClientException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class JFluxHttpClientIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";

    private static final String DB_NAME = "test_" + System.currentTimeMillis();
    private static final String RP_NAME = "test_retention_policy";

    private final JFluxHttpClient client = new JFluxHttpClient.Builder(INFLUX_DB_URL).build();

    @BeforeEach
    public void setup() throws IOException {
        client.execute("CREATE DATABASE " + DB_NAME);

        // @formatter:off
		String createRPStatement = "CREATE RETENTION POLICY " + RP_NAME
				+ " ON " + DB_NAME
				+ " DURATION 1d "
				+ " REPLICATION 1";
		// @formatter:on
        client.execute(createRPStatement);
    }

    @AfterEach
    public void tearDown() throws IOException {
        client.execute("DROP DATABASE " + DB_NAME);
    }

    @Test
    public void testPing() throws IOException {
        ResponseMetadata metadata = client.ping();
        assertThat(metadata.getTimestamp()).isNotNull();
        assertThat(metadata.getRequestId()).isNotBlank();
        assertThat(metadata.getDbBuildType()).isNotNull();
        assertThat(metadata.getDbVersion()).isNotNull();
    }

    @Test
    public void testQuery() throws IOException {
        Measurement measurement = client.query("SHOW DATABASES");
        assertThat(measurement).isNotNull();
    }

    @Test
    public void testQueryWithNoResults() throws IOException {
        Measurement measurement = client.query("SHOW MEASUREMENTS ON non_existent_db");
        assertThat(measurement).isNull();
    }

    @Test
    public void testSyntaxError() {
        assertThatExceptionOfType(InfluxClientException.class).isThrownBy(
                () -> client.query("SHOW DATABASE"));
    }

    @Test
    public void testQueryError() {
        assertThatExceptionOfType(InfluxClientException.class).isThrownBy(
                () -> client.query("SHOW RETENTION POLICIES ON non_existent_db"));
    }

    @Test
    public void testStatementWithError() {
        // Trying to execute incomplete statement.
        assertThatExceptionOfType(InfluxClientException.class).isThrownBy(
                () -> client.execute("CREATE RETENTION POLICY"));
    }

    @Test
    public void write_writesPoint_intoDefaultRP() throws IOException {
        // Given
        String measurementName = "my_measurement";
        String lineProtocol = measurementName + ",my_tag=1 my_field=" + System.currentTimeMillis();

        // When
        client.write(DB_NAME, lineProtocol);

        // Then
        Measurement result = client.query("SELECT * FROM " + DB_NAME + ".." + measurementName);
        assertThat(result).isNotNull();
        assertThat(result.getPoints()).hasSize(1);
    }

    @Test
    public void write_writesMultiplePoints() throws IOException {
        // Given
        String measurementName = "my_measurement";
        String tagName = "my_tag";
        String fieldName = "my_field";

        String firstPointLineProtocol = measurementName + "," + tagName + "=1 " + fieldName + "="
                + System.currentTimeMillis();
        String secondPointLineProtocol = measurementName + "," + tagName + "=2 " + fieldName + "="
                + System.currentTimeMillis();

        // When
        client.write(DB_NAME, firstPointLineProtocol + "\n" + secondPointLineProtocol);

        // Then
        Measurement result = client.query("SELECT * FROM " + DB_NAME + ".." + measurementName);
        assertThat(result).isNotNull();
        assertThat(result.getPoints()).hasSize(2);
    }

    @Test
    public void write_writesPoint_intoSpecificRP() throws IOException {
        // Given
        String measurementName = "my_measurement";
        String lineProtocol = measurementName + ",my_tag=1 my_field=" + System.currentTimeMillis();

        // When
        client.write(DB_NAME, RP_NAME, lineProtocol);

        // Then
        Measurement result =
                client.query("SELECT * FROM " + DB_NAME + "." + RP_NAME + "." + measurementName);
        assertThat(result).isNotNull();
        assertThat(result.getPoints()).hasSize(1);
    }

    @Test
    public void write_throwsException_ifLineProtocolIsInvalid() {
        assertThatExceptionOfType(InfluxClientException.class).isThrownBy(
                () -> client.write(DB_NAME, "my_measurement,tag=1,field=1"));
    }
}
