package com.nickrammos.jflux;

import java.time.Duration;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.domain.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@RunWith(MockitoJUnitRunner.class)
public class RetentionPolicyManagerTest {

    @Mock
    private JFluxHttpClient httpClient;

    private RetentionPolicyManager retentionPolicyManager;

    @Before
    public void setup() {
        retentionPolicyManager = new RetentionPolicyManager(httpClient);
    }

    @Test
    public void getRetentionPolicies_shouldThrowException_ifDatabaseNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.getRetentionPolicies(null));
    }

    @Test
    public void getRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.getRetentionPolicy(null, "some_db"));
    }

    @Test
    public void getRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.getRetentionPolicy("some_rp", null));
    }

    @Test
    public void retentionPolicyExists_shouldThrowException_ifRetentionPolicyNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.retentionPolicyExists(null, "some_db"));
    }

    @Test
    public void retentionPolicyExists_shouldThrowException_ifDatabaseNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.retentionPolicyExists("some_rp", null));
    }

    @Test
    public void createRetentionPolicy_shouldThrowException_ifRetentionPolicyIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.createRetentionPolicy(null, "some_db"));
    }

    @Test
    public void createRetentionPolicy_shouldThrowException_ifDatabaseIsNull() {
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("test_rp", Duration.ZERO).build();
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.createRetentionPolicy(retentionPolicy, null));
    }

    @Test
    public void alterRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("some_rp", Duration.ZERO).build();
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.alterRetentionPolicy(null, "some_db", newDefinition));
    }

    @Test
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("some_rp", Duration.ZERO).build();
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.alterRetentionPolicy("some_rp", null, newDefinition));
    }

    @Test
    public void alterRetentionPolicy_shouldThrowException_ifNewDefinitionIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.alterRetentionPolicy("some_rp", "some_db", null));
    }

    @Test
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.dropRetentionPolicy(null, "some_db"));
    }

    @Test
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> retentionPolicyManager.dropRetentionPolicy("some_rp", null));
    }
}