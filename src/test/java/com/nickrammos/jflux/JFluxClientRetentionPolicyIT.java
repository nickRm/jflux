package com.nickrammos.jflux;

import java.time.Duration;
import java.util.List;

import com.nickrammos.jflux.domain.RetentionPolicy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFluxClientRetentionPolicyIT extends AbstractJFluxClientIT {

    @Test
    public void getRetentionPolicies_shouldShowResults() {
        List<RetentionPolicy> retentionPolicies = jFluxClient.getRetentionPolicies(dbName);
        assertThat(retentionPolicies).isNotEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicies_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.getRetentionPolicies("non_existent_db");
    }

    @Test
    public void getRetentionPolicy_shouldReturnRetentionPolicy() {
        RetentionPolicy retentionPolicy = jFluxClient.getRetentionPolicy("autogen", dbName);
        assertThat(retentionPolicy).isNotNull();
    }

    @Test
    public void getRetentionPolicy_shouldReturnNull_ifNotFound() {
        RetentionPolicy retentionPolicy = jFluxClient.getRetentionPolicy("non_existent_rp",
                dbName);
        assertThat(retentionPolicy).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.getRetentionPolicy("some_rp", "non_existent_db");
    }

    @Test
    public void retentionPolicyExists_shouldReturnTrue_ifRetentionPolicyExists() {
        boolean exists =
                jFluxClient.retentionPolicyExists("autogen", dbName);
        assertThat(exists).isTrue();
    }

    @Test
    public void retentionPolicyExists_shouldReturnFalse_ifRetentionPolicyDoesNotExist() {
        boolean exists = jFluxClient.retentionPolicyExists("non_existent_rp", dbName);
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
        jFluxClient.createRetentionPolicy(retentionPolicy, dbName);
        assertThat(jFluxClient.retentionPolicyExists(retentionPolicyName, dbName)).isTrue();

        // Alter
        Duration newDuration = duration.plusHours(1);
        RetentionPolicy newDefinition = retentionPolicy.withDuration(newDuration);
        jFluxClient.alterRetentionPolicy(retentionPolicyName, dbName, newDefinition);
        RetentionPolicy actual = jFluxClient.getRetentionPolicy(retentionPolicyName, dbName);
        assertThat(actual.getDuration()).isEqualTo(newDuration);

        // Drop
        jFluxClient.dropRetentionPolicy(retentionPolicyName, dbName);
        assertThat(jFluxClient.retentionPolicyExists(retentionPolicyName, dbName)).isFalse();
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
        jFluxClient.createRetentionPolicy(retentionPolicy, dbName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();
        jFluxClient.alterRetentionPolicy("non_existent_rp", dbName, newDefinition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();
        jFluxClient.alterRetentionPolicy("autogen", "non_existent_db", newDefinition);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        jFluxClient.dropRetentionPolicy("non_existent_rp", dbName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        jFluxClient.dropRetentionPolicy("some_rp", "non_existent_db");
    }
}
