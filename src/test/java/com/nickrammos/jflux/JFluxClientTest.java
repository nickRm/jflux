package com.nickrammos.jflux;

import java.io.IOException;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Point;

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

    @Mock
    private RetentionPolicyManager retentionPolicyManager;

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        when(httpClient.ping()).thenReturn(new ResponseMetadata.Builder().build());
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
