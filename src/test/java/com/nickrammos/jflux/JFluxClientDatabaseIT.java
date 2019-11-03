package com.nickrammos.jflux;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFluxClientDatabaseIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        jFluxClient = new JFluxClient.Builder(INFLUX_DB_URL).build();
    }

    @Test
    public void showDatabases_shouldShowResults() {
        // Given/When
        List<String> databases = jFluxClient.getDatabases();

        // Then
        assertThat(databases).contains(DatabaseManager.INTERNAL_DATABASE_NAME);
    }

    @Test
    public void databaseExists_shouldReturnTrue_forExistingDatabase() {
        // Given/When
        boolean exists = jFluxClient.databaseExists(DatabaseManager.INTERNAL_DATABASE_NAME);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    public void databaseExists_shouldReturnFalse_forNonExistentDatabase() {
        // Given/When
        boolean exists = jFluxClient.databaseExists("non_existent_db");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    public void testCreateAndDropDatabase() {
        String databaseName = "test_db_" + System.currentTimeMillis();

        jFluxClient.createDatabase(databaseName);
        assertThat(jFluxClient.databaseExists(databaseName)).isTrue();

        jFluxClient.dropDatabase(databaseName);
        assertThat(jFluxClient.databaseExists(databaseName)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDatabase_shouldThrowException_ifDatabaseAlreadyExists() {
        jFluxClient.createDatabase(DatabaseManager.INTERNAL_DATABASE_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropDatabase_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.dropDatabase("non_existent_db");
    }
}
