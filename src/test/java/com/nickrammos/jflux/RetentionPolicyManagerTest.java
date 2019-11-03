package com.nickrammos.jflux;

import java.time.Duration;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.domain.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RetentionPolicyManagerTest {

    @Mock
    private JFluxHttpClient httpClient;

    private RetentionPolicyManager retentionPolicyManager;

    @Before
    public void setup() {
        retentionPolicyManager = new RetentionPolicyManager(httpClient);
    }

    @Test(expected = NullPointerException.class)
    public void getRetentionPolicies_shouldThrowException_ifDatabaseNameIsNull() {
        retentionPolicyManager.getRetentionPolicies(null);
    }

    @Test(expected = NullPointerException.class)
    public void getRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        retentionPolicyManager.getRetentionPolicy(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void getRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        retentionPolicyManager.getRetentionPolicy("some_rp", null);
    }

    @Test(expected = NullPointerException.class)
    public void retentionPolicyExists_shouldThrowException_ifRetentionPolicyNameIsNull() {
        retentionPolicyManager.retentionPolicyExists(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void retentionPolicyExists_shouldThrowException_ifDatabaseNameIsNull() {
        retentionPolicyManager.retentionPolicyExists("some_rp", null);
    }

    @Test(expected = NullPointerException.class)
    public void createRetentionPolicy_shouldThrowException_ifRetentionPolicyIsNull() {
        retentionPolicyManager.createRetentionPolicy(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void createRetentionPolicy_shouldThrowException_ifDatabaseIsNull() {
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("test_rp", Duration.ZERO).build();
        retentionPolicyManager.createRetentionPolicy(retentionPolicy, null);
    }

    @Test(expected = NullPointerException.class)
    public void alterRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("some_rp", Duration.ZERO).build();
        retentionPolicyManager.alterRetentionPolicy(null, "some_db", newDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("some_rp", Duration.ZERO).build();
        retentionPolicyManager.alterRetentionPolicy("some_rp", null, newDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void alterRetentionPolicy_shouldThrowException_ifNewDefinitionIsNull() {
        retentionPolicyManager.alterRetentionPolicy("some_rp", "some_db", null);
    }

    @Test(expected = NullPointerException.class)
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        retentionPolicyManager.dropRetentionPolicy(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        retentionPolicyManager.dropRetentionPolicy("some_rp", null);
    }
}