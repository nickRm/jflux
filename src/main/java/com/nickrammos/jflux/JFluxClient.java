package com.nickrammos.jflux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.RetentionPolicy;

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

    static final String INTERNAL_DATABASE_NAME = "_internal";

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

    /**
     * Gets the existing databases.
     *
     * @return the existing databases, should always contain at least the internal one
     */
    public List<String> getDatabases() {
        Measurement queryResult = callApi(() -> httpClient.query("SHOW DATABASES"));
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
    public boolean databaseExists(String databaseName) {
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
     * @throws IllegalArgumentException if the database already exists.
     */
    public void createDatabase(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("Database name cannot be null");
        }

        if (databaseExists(databaseName)) {
            throw new IllegalArgumentException("Database " + databaseName + " already exists");
        }

        callApi(() -> httpClient.execute("CREATE DATABASE \"" + databaseName + "\""));
        LOGGER.info("Created database '{}'", databaseName);
    }

    /**
     * Drops the specified database.
     *
     * @param databaseName the database to drop, not {@code null}
     *
     * @throws NullPointerException     if {@code databaseName} is {@code null}
     * @throws IllegalArgumentException if the database does not exist
     * @throws IllegalArgumentException if trying to drop the internal InfluxDB database
     */
    public void dropDatabase(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("Database cannot be null");
        }

        if (INTERNAL_DATABASE_NAME.equals(databaseName)) {
            throw new IllegalArgumentException("Cannot drop internal database");
        }

        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        callApi(() -> httpClient.execute("DROP DATABASE \"" + databaseName + "\""));
        LOGGER.info("Dropped database '{}'", databaseName);
    }

    /**
     * Gets all the retention policies defined on the specified database.
     *
     * @param databaseName name of the database to check, not {@code null}
     *
     * @return the database's retention policies
     *
     * @throws NullPointerException     if {@code databaseName} is {@code null}
     * @throws IllegalArgumentException if the database does not exist
     */
    public List<RetentionPolicy> getRetentionPolicies(String databaseName) {
        if (databaseName == null) {
            throw new NullPointerException("Database name cannot be null");
        }

        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        String query = "SHOW RETENTION POLICIES ON \"" + databaseName + "\"";
        Measurement queryResult = callApi(() -> httpClient.query(query));

        RetentionPolicyConverter converter = new RetentionPolicyConverter();
        List<RetentionPolicy> retentionPolicies = queryResult.getPoints()
                .stream()
                .map(converter::parsePoint)
                .collect(Collectors.toList());
        LOGGER.debug("Found retention policies {} on '{}'", retentionPolicies, databaseName);
        return retentionPolicies;
    }

    /**
     * Gets the definition of the specified retention policy.
     *
     * @param retentionPolicyName the retention policy to get
     * @param databaseName        the database the retention policy is defined on
     *
     * @return the retention policy definition, or {@code null} if not found
     *
     * @throws NullPointerException     if {@code retentionPolicyName} or {@code databaseName} are
     *                                  {@code null}
     * @throws IllegalArgumentException if the database does not exist
     */
    public RetentionPolicy getRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (retentionPolicyName == null) {
            throw new NullPointerException("Retention policy cannot be null");
        }

        return getRetentionPolicies(databaseName).stream()
                .filter(rp -> rp.getName().equals(retentionPolicyName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a value indicating whether the specified retention policy exists on the specified
     * database.
     *
     * @param retentionPolicyName the retention policy to check
     * @param databaseName        the database to check
     *
     * @return {@code true} if the retention policy exists, {@code false} otherwise
     *
     * @throws NullPointerException     if the retention policy or database names are null
     * @throws IllegalArgumentException if the database does not exist
     */
    public boolean retentionPolicyExists(String retentionPolicyName, String databaseName) {
        return getRetentionPolicy(retentionPolicyName, databaseName) != null;
    }

    /**
     * Creates a new retention policy on the specified database.
     *
     * @param retentionPolicy the retention policy to create
     * @param databaseName    the database to create the retention policy on
     *
     * @throws NullPointerException     if {@code retentionPolicy} or {@code databaseName} is {@code
     *                                  null}
     * @throws IllegalArgumentException if the database does not exist
     * @throws IllegalArgumentException if the retention policy already exists
     */
    public void createRetentionPolicy(RetentionPolicy retentionPolicy, String databaseName) {
        if (retentionPolicy == null) {
            throw new NullPointerException("Retention policy cannot be null");
        }

        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        if (retentionPolicyExists(retentionPolicy.getName(), databaseName)) {
            throw new IllegalArgumentException(
                    "Retention policy " + retentionPolicy.getName() + " already exists on "
                            + databaseName);
        }

        DurationConverter durationConverter = new DurationConverter();
        String statement = "CREATE RETENTION POLICY \"" + retentionPolicy.getName() + '"'
                + " ON \"" + databaseName + '"'
                + " DURATION " + durationConverter.toLiteral(retentionPolicy.getDuration())
                + " REPLICATION " + retentionPolicy.getReplication()
                + " SHARD DURATION " + durationConverter.toLiteral(
                retentionPolicy.getShardDuration())
                + (retentionPolicy.isDefault() ? " DEFAULT" : "");
        callApi(() -> httpClient.execute(statement));
        LOGGER.info("Created retention policy {} on '{}'", retentionPolicy, databaseName);
    }

    /**
     * Alters the specified retention policy.
     * <p>
     * Note that the name of the retention policy cannot be altered and thus the original name will
     * always be kept, even if the new definition specifies a different one.
     *
     * @param retentionPolicyName the retention policy to alter
     * @param databaseName        the database the retention policy is defined on
     * @param newDefinition       the new definition for the retention policy
     *
     * @throws NullPointerException     if any of the arguments are {@code null}
     * @throws IllegalArgumentException if the database does not exist
     * @throws IllegalArgumentException if the retention policy does not exist
     */
    public void alterRetentionPolicy(String retentionPolicyName, String databaseName,
            RetentionPolicy newDefinition) {
        if (newDefinition == null) {
            throw new NullPointerException("Retention policy definition cannot be null");
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new IllegalArgumentException("Unknown retention policy " + retentionPolicyName);
        }

        if (!retentionPolicyName.equals(newDefinition.getName())) {
            LOGGER.warn("Retention policy name cannot be altered, will remain '{}'",
                    retentionPolicyName);
        }

        DurationConverter durationConverter = new DurationConverter();
        String statement = "ALTER RETENTION POLICY \"" + retentionPolicyName + '"'
                + " ON \"" + databaseName + '"'
                + " DURATION " + durationConverter.toLiteral(newDefinition.getDuration())
                + " REPLICATION " + newDefinition.getReplication()
                + " SHARD DURATION " + durationConverter.toLiteral(
                newDefinition.getShardDuration())
                + (newDefinition.isDefault() ? " DEFAULT" : "");
        callApi(() -> httpClient.execute(statement));
        LOGGER.info("Updated '{}'.'{}' to {}", databaseName, retentionPolicyName, newDefinition);
    }

    /**
     * Drops the specified retention policy.
     *
     * @param retentionPolicyName the retention policy to drop
     * @param databaseName        the database the retention policy is defined on
     *
     * @throws NullPointerException     if {@code retentionPolicy} or {@code databaseName} are
     *                                  {@code null}
     * @throws IllegalArgumentException if either the retention policy or the database do not exist
     */
    public void dropRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new IllegalArgumentException("Unknown retention policy " + retentionPolicyName);
        }

        String statement =
                "DROP RETENTION POLICY \"" + retentionPolicyName + "\" ON \"" + databaseName + '"';
        callApi(() -> httpClient.execute(statement));
        LOGGER.info("Dropped retention policy '{}' on '{}'", retentionPolicyName, databaseName);
    }

    private void callApi(IOThrowingRunnable apiMethod) {
        try {
            apiMethod.run();
        } catch (IOException e) {
            throw new IllegalStateException("Connection to InfluxDB lost", e);
        }
    }

    private <T> T callApi(IOThrowingSupplier<T> apiMethod) {
        try {
            return apiMethod.get();
        } catch (IOException e) {
            throw new IllegalStateException("Connection to InfluxDB lost", e);
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

    /**
     * Convenience interface for runnables that throw IOExceptions.
     */
    private interface IOThrowingRunnable {

        void run() throws IOException;
    }

    /**
     * Convenience interface for suppliers that throw IOExceptions.
     *
     * @param <T> type of the return value
     */
    private interface IOThrowingSupplier<T> {

        T get() throws IOException;
    }
}
