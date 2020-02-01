package com.github.nickrm.jflux;

import java.io.IOException;
import java.util.List;

import com.github.nickrm.jflux.exception.DatabaseAlreadyExistsException;
import com.github.nickrm.jflux.exception.UnknownDatabaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class JFluxClientDatabaseIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";

    private JFluxClient jFluxClient;

    @BeforeEach
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

    @Test
    public void createDatabase_shouldThrowException_ifDatabaseAlreadyExists() {
        assertThatExceptionOfType(DatabaseAlreadyExistsException.class).isThrownBy(
                () -> jFluxClient.createDatabase(DatabaseManager.INTERNAL_DATABASE_NAME));
    }

    @Test
    public void dropDatabase_shouldThrowException_ifDatabaseDoesNotExist() {
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.dropDatabase("non_existent_db"));
    }
}
