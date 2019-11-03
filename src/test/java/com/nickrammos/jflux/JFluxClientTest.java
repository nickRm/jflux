package com.nickrammos.jflux;

import java.io.IOException;
import java.time.Duration;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.RetentionPolicy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JFluxClientTest {

    @Mock
    private JFluxHttpClient httpClient;

    @Mock
    private DatabaseManager databaseManager;

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        when(httpClient.ping()).thenReturn(new ResponseMetadata.Builder().build());
        jFluxClient = new JFluxClient(httpClient, databaseManager);
    }

    @Test(expected = IOException.class)
    public void ctor_shouldThrowException_ifInfluxDBUnreachable() throws IOException {
        // Given
        doThrow(new IOException()).when(httpClient).ping();

        // When
        new JFluxClient(httpClient, databaseManager);

        // Then
        // Expect exception.
    }

    @Test(expected = NullPointerException.class)
    public void getRetentionPolicies_shouldThrowException_ifDatabaseNameIsNull() {
        jFluxClient.getRetentionPolicies(null);
    }

    @Test(expected = NullPointerException.class)
    public void getRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        jFluxClient.getRetentionPolicy(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void getRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        jFluxClient.getRetentionPolicy("some_rp", null);
    }

    @Test(expected = NullPointerException.class)
    public void retentionPolicyExists_shouldThrowException_ifRetentionPolicyNameIsNull() {
        jFluxClient.retentionPolicyExists(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void retentionPolicyExists_shouldThrowException_ifDatabaseNameIsNull() {
        jFluxClient.retentionPolicyExists("some_rp", null);
    }

    @Test(expected = NullPointerException.class)
    public void createRetentionPolicy_shouldThrowException_ifRetentionPolicyIsNull() {
        jFluxClient.createRetentionPolicy(null, "some_db");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRetentionPolicy_shouldThrowException_ifDatabaseIsNull() {
        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("test_rp", Duration.ZERO).build();
        jFluxClient.createRetentionPolicy(retentionPolicy, null);
    }

    @Test(expected = NullPointerException.class)
    public void alterRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("some_rp", Duration.ZERO).build();
        jFluxClient.alterRetentionPolicy(null, "some_db", newDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("some_rp", Duration.ZERO).build();
        jFluxClient.alterRetentionPolicy("some_rp", null, newDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void alterRetentionPolicy_shouldThrowException_ifNewDefinitionIsNull() {
        jFluxClient.alterRetentionPolicy("some_rp", "some_db", null);
    }

    @Test(expected = NullPointerException.class)
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyNameIsNull() {
        jFluxClient.dropRetentionPolicy(null, "some_db");
    }

    @Test(expected = NullPointerException.class)
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseNameIsNull() {
        jFluxClient.dropRetentionPolicy("some_rp", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void write_shouldThrowException_ifInputIsNull() {
        jFluxClient.write("some_db", (Object) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeToRetentionPolicy_shouldThrowException_ifInputIsNull() {
        jFluxClient.write("some_db", "some_rp", (Object) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writePoint_shouldThrowException_ifDatabaseNameIsNull() {
        jFluxClient.writePoint(null, "some_measurement", new Point.Builder().build());
    }

    @Test
    public void close_shouldAlsoCloseHttpClient() throws Exception {
        // Given/When
        jFluxClient.close();

        // Then
        verify(httpClient).close();
    }
}
