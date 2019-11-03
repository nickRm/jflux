package com.nickrammos.jflux;

import java.util.ArrayList;
import java.util.List;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.domain.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles database management functionality.
 */
final class DatabaseManager {

    static final String INTERNAL_DATABASE_NAME = "_internal";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    private final JFluxHttpClient httpClient;
    private final ApiCaller apiCaller;

    DatabaseManager(JFluxHttpClient httpClient) {
        this.httpClient = httpClient;
        apiCaller = new ApiCaller();
    }

    /**
     * Gets the existing databases.
     *
     * @return the existing databases, should always contain at least the internal one
     */
    List<String> getDatabases() {
        Measurement queryResult = apiCaller.callApi(() -> httpClient.query("SHOW DATABASES"));
        List<String> databases = new ArrayList<>();
        for (Point point : queryResult.getPoints()) {
            databases.addAll(point.getTags().values());
        }
        LOGGER.debug("Found databases: {}", databases);
        return databases;
    }

    /**
     * Gets a value indicating whether the specified database exists.
     *
     * @param databaseName the database name to check, not {@code null}
     *
     * @return {@code true} if the database exists, {@code false} otherwise
     *
     * @throws NullPointerException if {@code databaseName} is {@code null}
     */
    boolean databaseExists(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("Database name cannot be null");
        }
        return getDatabases().contains(databaseName);
    }

    /**
     * Creates a new database with the specified name.
     *
     * @param databaseName the database to create, not {@code null}
     *
     * @throws NullPointerException     if {@code databaseName} is {@code null}
     */
    void createDatabase(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("Database name cannot be null");
        }

        apiCaller.callApi(() -> httpClient.execute("CREATE DATABASE \"" + databaseName + "\""));
        LOGGER.info("Created database '{}'", databaseName);
    }

    /**
     * Drops the specified database.
     *
     * @param databaseName the database to drop, not {@code null}
     *
     * @throws NullPointerException     if {@code databaseName} is {@code null}
     * @throws IllegalArgumentException if trying to drop the internal InfluxDB database
     */
    void dropDatabase(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("Database cannot be null");
        }

        if (INTERNAL_DATABASE_NAME.equals(databaseName)) {
            throw new IllegalArgumentException("Cannot drop internal database");
        }

        apiCaller.callApi(() -> httpClient.execute("DROP DATABASE \"" + databaseName + "\""));
        LOGGER.info("Dropped database '{}'", databaseName);
    }
}
