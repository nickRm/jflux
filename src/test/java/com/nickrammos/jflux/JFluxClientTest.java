package com.nickrammos.jflux;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.BuildType;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.RetentionPolicy;
import com.nickrammos.jflux.domain.Version;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JFluxClientTest {

    @Mock
    private JFluxHttpClient httpClient;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private RetentionPolicyManager retentionPolicyManager;

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        ResponseMetadata pingResponse =
                new ResponseMetadata.Builder().dbBuildType(BuildType.OPEN_SOURCE)
                        .dbVersion(Version.fromString("0.0.0"))
                        .build();
        when(httpClient.ping()).thenReturn(pingResponse);
        when(httpClient.getHostUrl()).thenReturn("test-mock");

        jFluxClient = new JFluxClient(httpClient, databaseManager, retentionPolicyManager);
    }

    @Test(expected = IOException.class)
    public void ctor_shouldThrowException_ifInfluxDBUnreachable() throws IOException {
        // Given
        doThrow(new IOException()).when(httpClient).ping();

        // When
        new JFluxClient(httpClient, databaseManager, retentionPolicyManager);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDatabase_shouldThrowException_ifDatabaseExists() {
        // Given
        String databaseName = "some_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);

        // When
        jFluxClient.createDatabase(databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropDatabase_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        jFluxClient.dropDatabase(databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicies_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        jFluxClient.getRetentionPolicies(databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        jFluxClient.getRetentionPolicy("some_rp", databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void retentionPolicyExists_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        jFluxClient.retentionPolicyExists("autogen", databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("test_rp", Duration.ZERO).build();

        // When
        jFluxClient.createRetentionPolicy(retentionPolicy, databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void createRetentionPolicy_shouldThrowException_ifAlreadyExists() {
        // Given
        String databaseName = "some_db";
        String retentionPolicyName = "some_rp";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);
        when(retentionPolicyManager.retentionPolicyExists(retentionPolicyName,
                databaseName)).thenReturn(true);

        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder(retentionPolicyName, Duration.ZERO).build();

        // When
        jFluxClient.createRetentionPolicy(retentionPolicy, databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();

        // When
        jFluxClient.alterRetentionPolicy("autogen", databaseName, newDefinition);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void alterRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        // Given
        String databaseName = "some_db";
        String retentionPolicyName = "non_existent_rp";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);
        when(retentionPolicyManager.retentionPolicyExists(retentionPolicyName,
                databaseName)).thenReturn(false);

        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();

        // When
        jFluxClient.alterRetentionPolicy(retentionPolicyName, databaseName, newDefinition);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        jFluxClient.dropRetentionPolicy("some_rp", databaseName);

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        // Given
        String databaseName = "some_db";
        String retentionPolicyName = "non_existent_rp";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);
        when(retentionPolicyManager.retentionPolicyExists(retentionPolicyName,
                databaseName)).thenReturn(false);

        // When
        jFluxClient.dropRetentionPolicy(retentionPolicyName, databaseName);

        // Then
        // Expect exception.
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

    @Test(expected = IllegalArgumentException.class)
    public void writePoint_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        jFluxClient.writePoint(databaseName, "some_measurement", point);

        // Then
        // Exception should be thrown.
    }

    @Test(expected = IllegalArgumentException.class)
    public void writePointToRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        // Given
        String databaseName = "some_db";
        String retentionPolicyName = "non_existent_rp";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);
        when(retentionPolicyManager.retentionPolicyExists(retentionPolicyName,
                databaseName)).thenReturn(false);

        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        jFluxClient.writePoint(databaseName, "some_measurement", retentionPolicyName, point);

        // Then
        // Exception should be thrown.
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllPoints_shouldThrowException_ifDatabaseNameIsNull() {
        jFluxClient.getAllPoints(null, "some_measurement");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllPoints_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String dbName = "some_db";
        when(databaseManager.databaseExists(dbName)).thenReturn(false);

        // When
        jFluxClient.getAllPoints(dbName, "some_measurement");

        // Then
        // Expect exception.
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllPoints_shouldThrowException_ifMeasurementNameIsNull() {
        jFluxClient.getAllPoints("db_name", null);
    }

    @Test
    public void getAllPoints_shouldReturnEmptyList_ifNoResults() throws IOException {
        // Given
        String databaseName = "some_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);
        when(httpClient.query(anyString())).thenReturn(null);

        // When
        List<Point> points = jFluxClient.getAllPoints(databaseName, "non_existent_measurement");

        // Then
        assertThat(points).isEmpty();
    }

    @Test
    public void close_shouldAlsoCloseHttpClient() throws Exception {
        // Given/When
        jFluxClient.close();

        // Then
        verify(httpClient).close();
    }
}
