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
import com.nickrammos.jflux.exception.DatabaseAlreadyExistsException;
import com.nickrammos.jflux.exception.UnknownDatabaseException;
import com.nickrammos.jflux.exception.UnknownRetentionPolicyException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JFluxClientTest {

    @Mock
    private JFluxHttpClient httpClient;

    @Mock
    private DatabaseManager databaseManager;

    @Mock
    private RetentionPolicyManager retentionPolicyManager;

    private JFluxClient jFluxClient;

    @BeforeEach
    public void setup() throws IOException {
        ResponseMetadata pingResponse =
                new ResponseMetadata.Builder().dbBuildType(BuildType.OPEN_SOURCE)
                        .dbVersion(Version.fromString("0.0.0"))
                        .build();
        when(httpClient.ping()).thenReturn(pingResponse);
        when(httpClient.getHostUrl()).thenReturn("test-mock");

        jFluxClient = new JFluxClient(httpClient, databaseManager, retentionPolicyManager);
    }

    @Test
    public void ctor_shouldThrowException_ifInfluxDBUnreachable() throws IOException {
        // Given
        doThrow(new IOException()).when(httpClient).ping();

        // When
        assertThatIOException().isThrownBy(
                () -> new JFluxClient(httpClient, databaseManager, retentionPolicyManager));
    }

    @Test
    public void createDatabase_shouldThrowException_ifDatabaseExists() {
        // Given
        String databaseName = "some_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);

        // When
        assertThatExceptionOfType(DatabaseAlreadyExistsException.class).isThrownBy(
                () -> jFluxClient.createDatabase(databaseName));
    }

    @Test
    public void dropDatabase_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.dropDatabase(databaseName));
    }

    @Test
    public void getRetentionPolicies_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.getRetentionPolicies(databaseName));
    }

    @Test
    public void getRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.getRetentionPolicy("some_rp", databaseName));
    }

    @Test
    public void retentionPolicyExists_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.retentionPolicyExists("autogen", databaseName));
    }

    @Test
    public void createRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        RetentionPolicy retentionPolicy =
                new RetentionPolicy.Builder("test_rp", Duration.ZERO).build();

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.createRetentionPolicy(retentionPolicy, databaseName));
    }

    @Test
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
        assertThatIllegalArgumentException().isThrownBy(
                () -> jFluxClient.createRetentionPolicy(retentionPolicy, databaseName));
    }

    @Test
    public void alterRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        RetentionPolicy newDefinition =
                new RetentionPolicy.Builder("non_existent_rp", Duration.ZERO).build();

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.alterRetentionPolicy("autogen", databaseName, newDefinition));
    }

    @Test
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
        assertThatExceptionOfType(UnknownRetentionPolicyException.class).isThrownBy(
                () -> jFluxClient.alterRetentionPolicy(retentionPolicyName, databaseName,
                        newDefinition));
    }

    @Test
    public void dropRetentionPolicy_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.dropRetentionPolicy("some_rp", databaseName));
    }

    @Test
    public void dropRetentionPolicy_shouldThrowException_ifRetentionPolicyDoesNotExist() {
        // Given
        String databaseName = "some_db";
        String retentionPolicyName = "non_existent_rp";
        when(databaseManager.databaseExists(databaseName)).thenReturn(true);
        when(retentionPolicyManager.retentionPolicyExists(retentionPolicyName,
                databaseName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownRetentionPolicyException.class).isThrownBy(
                () -> jFluxClient.dropRetentionPolicy(retentionPolicyName, databaseName));
    }

    @Test
    public void write_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> jFluxClient.write("some_db", (Object) null));
    }

    @Test
    public void writeToRetentionPolicy_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> jFluxClient.write("some_db", "some_rp", (Object) null));
    }

    @Test
    public void writePoint_shouldThrowException_ifDatabaseNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> jFluxClient.writePoint(null, "some_measurement",
                        new Point.Builder().build()));
    }

    @Test
    public void writePoint_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String databaseName = "non_existent_db";
        when(databaseManager.databaseExists(databaseName)).thenReturn(false);

        Point point = new Point.Builder().fields(Collections.singletonMap("some_field", 1))
                .tags(Collections.singletonMap("some_tag", "tag value"))
                .build();

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.writePoint(databaseName, "some_measurement", point));
    }

    @Test
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
        assertThatExceptionOfType(UnknownRetentionPolicyException.class).isThrownBy(
                () -> jFluxClient.writePoint(databaseName, "some_measurement", retentionPolicyName,
                        point));
    }

    @Test
    public void getAllPoints_shouldThrowException_ifDatabaseNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> jFluxClient.getAllPoints(null, "some_measurement"));
    }

    @Test
    public void getAllPoints_shouldThrowException_ifDatabaseDoesNotExist() {
        // Given
        String dbName = "some_db";
        when(databaseManager.databaseExists(dbName)).thenReturn(false);

        // When
        assertThatExceptionOfType(UnknownDatabaseException.class).isThrownBy(
                () -> jFluxClient.getAllPoints(dbName, "some_measurement"));
    }

    @Test
    public void getAllPoints_shouldThrowException_ifMeasurementNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> jFluxClient.getAllPoints("db_name", null));
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
