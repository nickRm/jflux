package com.nickrammos.jflux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.nickrammos.jflux.domain.RetentionPolicy;

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
        assertThat(databases).contains(JFluxClient.INTERNAL_DATABASE_NAME);
    }

    @Test
    public void databaseExists_shouldReturnTrue_forExistingDatabase() {
        // Given/When
        boolean exists = jFluxClient.databaseExists(JFluxClient.INTERNAL_DATABASE_NAME);

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
    public void testCreateAndDropDatabase() {
        String databaseName = "test_db_" + System.currentTimeMillis();

        jFluxClient.createDatabase(databaseName);
        assertThat(jFluxClient.databaseExists(databaseName)).isTrue();

        jFluxClient.dropDatabase(databaseName);
        assertThat(jFluxClient.databaseExists(databaseName)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDatabase_shouldThrowException_ifDatabaseAlreadyExists() {
        jFluxClient.createDatabase(JFluxClient.INTERNAL_DATABASE_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropDatabase_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.dropDatabase("non_existent_db");
    }

    @Test
    public void getRetentionPolicies_shouldShowResults() {
        List<RetentionPolicy> retentionPolicies =
                jFluxClient.getRetentionPolicies(JFluxClient.INTERNAL_DATABASE_NAME);
        assertThat(retentionPolicies).isNotEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicies_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.getRetentionPolicies("non_existent_db");
    }

    @Test
    public void getRetentionPolicy_shouldReturnRetentionPolicy() {
        RetentionPolicy retentionPolicy =
                jFluxClient.getRetentionPolicy("monitor", JFluxClient.INTERNAL_DATABASE_NAME);
        assertThat(retentionPolicy).isNotNull();
    }

    @Test
    public void getRetentionPolicy_shouldReturnNull_ifNotFound() {
        RetentionPolicy retentionPolicy = jFluxClient.getRetentionPolicy("non_existent_rp",
                JFluxClient.INTERNAL_DATABASE_NAME);
        assertThat(retentionPolicy).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.getRetentionPolicy("some_rp", "non_existent_db");
    }

    @Test
    public void retentionPolicyExists_shouldReturnTrue_ifRetentionPolicyExists() {
        boolean exists =
                jFluxClient.retentionPolicyExists("monitor", JFluxClient.INTERNAL_DATABASE_NAME);
        assertThat(exists).isTrue();
    }

    @Test
    public void retentionPolicyExists_shouldReturnFalse_ifRetentionPolicyDoesNotExist() {
        boolean exists =
                jFluxClient.retentionPolicyExists("non_existent_rp",
                        JFluxClient.INTERNAL_DATABASE_NAME);
        assertThat(exists).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void retentionPolicyExists_shouldThrowException_ifDatabaseDoesNotExist() {
        boolean exists = jFluxClient.retentionPolicyExists("autogen", "non_existent_db");
        assertThat(exists).isFalse();
    }

    @Test
    public void testCreateAndDropRetentionPolicy() {
        String retentionPolicyName = "test_rp_" + System.currentTimeMillis();
        String databaseName = JFluxClient.INTERNAL_DATABASE_NAME;
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder(retentionPolicyName, Duration.ofHours(1)).build();

        jFluxClient.createRetentionPolicy(retentionPolicy, databaseName);
        assertThat(jFluxClient.retentionPolicyExists(retentionPolicyName, databaseName)).isTrue();

        jFluxClient.dropRetentionPolicy(retentionPolicyName, databaseName);
        assertThat(jFluxClient.retentionPolicyExists(retentionPolicyName, databaseName)).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("test_rp", Duration.ZERO).build();
        jFluxClient.createRetentionPolicy(retentionPolicy, "non_existent_db");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRetentionPolicy_shouldThrowException_ifRetentionPolicyAlreadyExists() {
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("monitor", Duration.ZERO).build();
        jFluxClient.createRetentionPolicy(retentionPolicy, JFluxClient.INTERNAL_DATABASE_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        jFluxClient.dropRetentionPolicy("non_existent_rp", JFluxClient.INTERNAL_DATABASE_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.dropRetentionPolicy("some_rp", "non_existent_db");
    }
}
