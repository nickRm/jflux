package com.nickrammos.jflux;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.nickrammos.jflux.domain.RetentionPolicy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFluxClientRetentionPolicyIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";
    private static final String DB_NAME =
            JFluxClientRetentionPolicyIT.class.getSimpleName() + "_" + System.currentTimeMillis();

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
    public void getRetentionPolicies_shouldShowResults() {
        List<RetentionPolicy> retentionPolicies = jFluxClient.getRetentionPolicies(DB_NAME);
        assertThat(retentionPolicies).isNotEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicies_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.getRetentionPolicies("non_existent_db");
    }

    @Test
    public void getRetentionPolicy_shouldReturnRetentionPolicy() {
        RetentionPolicy retentionPolicy = jFluxClient.getRetentionPolicy("autogen", DB_NAME);
        assertThat(retentionPolicy).isNotNull();
    }

    @Test
    public void getRetentionPolicy_shouldReturnNull_ifNotFound() {
        RetentionPolicy retentionPolicy = jFluxClient.getRetentionPolicy("non_existent_rp",
                DB_NAME);
        assertThat(retentionPolicy).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.getRetentionPolicy("some_rp", "non_existent_db");
    }

    @Test
    public void retentionPolicyExists_shouldReturnTrue_ifRetentionPolicyExists() {
        boolean exists =
                jFluxClient.retentionPolicyExists("autogen", DB_NAME);
        assertThat(exists).isTrue();
    }

    @Test
    public void retentionPolicyExists_shouldReturnFalse_ifRetentionPolicyDoesNotExist() {
        boolean exists = jFluxClient.retentionPolicyExists("non_existent_rp", DB_NAME);
        assertThat(exists).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void retentionPolicyExists_shouldThrowException_ifDatabaseDoesNotExist() {
        boolean exists = jFluxClient.retentionPolicyExists("autogen", "non_existent_db");
        assertThat(exists).isFalse();
    }

    @Test
    public void testCreateAlterDropRetentionPolicy() {
        String retentionPolicyName = "test_rp_" + System.currentTimeMillis();
        Duration duration = Duration.ofHours(1);
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder(retentionPolicyName, duration).build();

        // Create
        jFluxClient.createRetentionPolicy(retentionPolicy, DB_NAME);
        assertThat(jFluxClient.retentionPolicyExists(retentionPolicyName, DB_NAME)).isTrue();

        // Alter
        Duration newDuration = duration.plusHours(1);
        RetentionPolicy newDefinition = retentionPolicy.withDuration(newDuration);
        jFluxClient.alterRetentionPolicy(retentionPolicyName, DB_NAME, newDefinition);
        RetentionPolicy actual = jFluxClient.getRetentionPolicy(retentionPolicyName, DB_NAME);
        assertThat(actual.getDuration()).isEqualTo(newDuration);

        // Drop
        jFluxClient.dropRetentionPolicy(retentionPolicyName, DB_NAME);
        assertThat(jFluxClient.retentionPolicyExists(retentionPolicyName, DB_NAME)).isFalse();
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
                new RetentionPolicy.Builder("autogen", Duration.ZERO).build();
        jFluxClient.createRetentionPolicy(retentionPolicy, DB_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();
        jFluxClient.alterRetentionPolicy("non_existent_rp", DB_NAME, newDefinition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();
        jFluxClient.alterRetentionPolicy("autogen", "non_existent_db", newDefinition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        jFluxClient.dropRetentionPolicy("non_existent_rp", DB_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.dropRetentionPolicy("some_rp", "non_existent_db");
    }
}
