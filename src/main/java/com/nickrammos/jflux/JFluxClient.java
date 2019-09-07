package com.nickrammos.jflux;

import java.io.IOException;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for InfluxDB.
 * <p>
 * Provides convenient abstractions over {@link JFluxHttpClient}.
 *
 * @see JFluxHttpClient
 */
public final class JFluxClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JFluxClient.class);

    private final JFluxHttpClient httpClient;

    /**
     * Initializes a new instance, setting the HTTP client used to query the InfluxDB API.
     *
     * @param httpClient the InfluxDB HTTP API client
     *
     * @throws IOException if the InfluxDB instance is unreachable
     * @see Builder
     */
    JFluxClient(JFluxHttpClient httpClient) throws IOException {
        this.httpClient = httpClient;

        try {
            ResponseMetadata responseMetadata = httpClient.ping();
            LOGGER.info("Connected to InfluxDB {} {} instance at {}",
                    responseMetadata.getDbBuildType(), responseMetadata.getDbVersion(),
                    httpClient.getHostUrl());
        } catch (IOException e) {
            throw new IOException("Could not connect to InfluxDB instance", e);
        }
    }

    @Override
    public void close() throws Exception {
        httpClient.close();
    }

    /**
     * Used to construct instances of {@link JFluxClient}.
     */
    public static final class Builder {

        private String host;

        /**
         * Initializes a new builder instance, setting the InfluxDB host URL.
         *
         * @param host the InfluxDB host URL, e.g. {@code http://localhost:8086}
         */
        public Builder(String host) {
            this.host = host;
        }

        /**
         * Constructs a new {@link JFluxClient} instance from this builder's configuration.
         *
         * @return the new client instance
         *
         * @throws IOException if the InfluxDB instance is unreachable
         */
        public JFluxClient build() throws IOException {
            JFluxHttpClient httpClient = new JFluxHttpClient.Builder(host).build();
            return new JFluxClient(httpClient);
        }
    }
}
