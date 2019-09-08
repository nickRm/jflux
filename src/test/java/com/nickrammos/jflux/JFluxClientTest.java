package com.nickrammos.jflux;

import java.io.IOException;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;

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

    private JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        when(httpClient.ping()).thenReturn(new ResponseMetadata.Builder().build());
        jFluxClient = new JFluxClient(httpClient);
    }

    @Test(expected = IOException.class)
    public void ctor_shouldThrowException_ifInfluxDBUnreachable() throws IOException {
        // Given
        doThrow(new IOException()).when(httpClient).ping();

        // When
        new JFluxClient(httpClient);

        // Then
        // Expect exception.
    }

    @Test(expected = NullPointerException.class)
    public void databaseExists_shouldThrowException_ifNameIsNull() {
        jFluxClient.databaseExists(null);
    }

    @Test(expected = NullPointerException.class)
    public void createDatabase_shouldThrowException_ifNameIsNull() {
        jFluxClient.createDatabase(null);
    }

    @Test(expected = NullPointerException.class)
    public void dropDatabase_shouldThrowException_ifNameIsNull() {
        jFluxClient.dropDatabase(null);
    }

    @Test
    public void close_shouldAlsoCloseHttpClient() throws Exception {
        // Given/When
        jFluxClient.close();

        // Then
        verify(httpClient).close();
    }
}
