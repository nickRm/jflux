package com.nickrammos.jflux;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFluxClientIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        jFluxClient = new JFluxClient.Builder(INFLUX_DB_URL).build();
    }

    @Test
    public void showDatabases_showsResults() {
        // Given/When
        List<String> databases = jFluxClient.getDatabases();

        // Then
        assertThat(databases).contains("_internal");
    }

    @Test
    public void databaseExists_shouldReturnTrue_forExistingDatabase() {
        // Given/When
        boolean exists = jFluxClient.databaseExists("_internal");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    public void databaseExists_shouldReturnTrue_forNonExistentDatabase() {
        // Given/When
        boolean exists = jFluxClient.databaseExists("non_existent_db");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    public void testCreateDatabase() {
        // Given
        String databaseName = "test_db_" + System.currentTimeMillis();

        // When
        jFluxClient.createDatabase(databaseName);

        // Then
        assertThat(jFluxClient.databaseExists(databaseName)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDatabase_shouldThrowException_ifDatabaseAlreadyExists() {
        jFluxClient.createDatabase("_internal");
    }
}
