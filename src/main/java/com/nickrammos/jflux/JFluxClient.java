package com.nickrammos.jflux;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.nickrammos.jflux.api.JFluxHttpClient;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.RetentionPolicy;
import com.nickrammos.jflux.exception.AnnotationProcessingException;

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
    private final DatabaseManager databaseManager;
    private final RetentionPolicyManager retentionPolicyManager;
    private final ApiCaller apiCaller;
    private final LineProtocolConverter lineProtocolConverter;
    private final NamingStrategy namingStrategy;
    private final AnnotationBasedPointConverter annotationBasedPointConverter;

    /**
     * Initializes a new instance, setting the required dependencies.
     *
     * @param httpClient             the InfluxDB HTTP API client
     * @param databaseManager        used for database management
     * @param retentionPolicyManager used for retention policy management
     *
     * @throws IOException if the InfluxDB instance is unreachable
     * @see Builder
     */
    JFluxClient(JFluxHttpClient httpClient, DatabaseManager databaseManager,
            RetentionPolicyManager retentionPolicyManager) throws IOException {
        this.httpClient = httpClient;
        this.databaseManager = databaseManager;
        this.retentionPolicyManager = retentionPolicyManager;

        try {
            ResponseMetadata responseMetadata = httpClient.ping();
            LOGGER.info("Connected to InfluxDB {} {} instance at {}",
                    responseMetadata.getDbBuildType(), responseMetadata.getDbVersion(),
                    httpClient.getHostUrl());
        } catch (IOException e) {
            throw new IOException("Could not connect to InfluxDB instance", e);
        }

        apiCaller = new ApiCaller();
        lineProtocolConverter = new LineProtocolConverter();
        namingStrategy = new NamingStrategy();
        annotationBasedPointConverter = new AnnotationBasedPointConverter(namingStrategy);
    }

    /**
     * Gets the existing databases.
     *
     * @return the existing databases, should always contain at least the internal one
     */
    public List<String> getDatabases() {
        return databaseManager.getDatabases();
    }

    /**
     * Gets a value indicating whether the specified database exists.
     *
     * @param databaseName the database name to check, not {@code null}
     *
     * @return {@code true} if the database exists, {@code false} otherwise
     *
     * @throws IllegalArgumentException if {@code databaseName} is {@code null}
     */
    public boolean databaseExists(String databaseName) {
        return databaseManager.databaseExists(databaseName);
    }

    /**
     * Creates a new database with the specified name.
     *
     * @param databaseName the database to create, not {@code null}
     *
     * @throws IllegalArgumentException if {@code databaseName} is {@code null}
     * @throws IllegalArgumentException if the database already exists
     */
    public void createDatabase(String databaseName) {
        if (databaseExists(databaseName)) {
            throw new IllegalArgumentException("Database " + databaseName + " already exists");
        }
        databaseManager.createDatabase(databaseName);
    }

    /**
     * Drops the specified database.
     *
     * @param databaseName the database to drop, not {@code null}
     *
     * @throws IllegalArgumentException if {@code databaseName} is {@code null}
     * @throws IllegalArgumentException if the database does not exist
     * @throws IllegalArgumentException if trying to drop the internal InfluxDB database
     */
    public void dropDatabase(String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Database " + databaseName + " already exists");
        }
        databaseManager.dropDatabase(databaseName);
    }

    /**
     * Gets all the retention policies defined on the specified database.
     *
     * @param databaseName name of the database to check, not {@code null}
     *
     * @return the database's retention policies
     *
     * @throws IllegalArgumentException if the database does not exist
     */
    public List<RetentionPolicy> getRetentionPolicies(String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }
        return retentionPolicyManager.getRetentionPolicies(databaseName);
    }

    /**
     * Gets the definition of the specified retention policy.
     *
     * @param retentionPolicyName the retention policy to get
     * @param databaseName        the database the retention policy is defined on
     *
     * @return the retention policy definition, or {@code null} if not found
     *
     * @throws IllegalArgumentException if {@code retentionPolicyName} or {@code databaseName} are
     *                                  {@code null}
     * @throws IllegalArgumentException if the database does not exist
     */
    public RetentionPolicy getRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }
        return retentionPolicyManager.getRetentionPolicy(retentionPolicyName, databaseName);
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
     * @throws IllegalArgumentException if the retention policy or database names are null
     * @throws IllegalArgumentException if the database does not exist
     */
    public boolean retentionPolicyExists(String retentionPolicyName, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }
        return retentionPolicyManager.retentionPolicyExists(retentionPolicyName, databaseName);
    }

    /**
     * Creates a new retention policy on the specified database.
     *
     * @param retentionPolicy the retention policy to create
     * @param databaseName    the database to create the retention policy on
     *
     * @throws IllegalArgumentException if {@code retentionPolicy} or {@code databaseName} is {@code
     *                                  null}
     * @throws IllegalArgumentException if the database does not exist
     * @throws IllegalArgumentException if the retention policy already exists
     */
    public void createRetentionPolicy(RetentionPolicy retentionPolicy, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        if (retentionPolicyExists(retentionPolicy.getName(), databaseName)) {
            throw new IllegalArgumentException(
                    "Retention policy " + retentionPolicy.getName() + " already exists on "
                            + databaseName);
        }

        retentionPolicyManager.createRetentionPolicy(retentionPolicy, databaseName);
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
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     * @throws IllegalArgumentException if the database does not exist
     * @throws IllegalArgumentException if the retention policy does not exist
     */
    public void alterRetentionPolicy(String retentionPolicyName, String databaseName,
            RetentionPolicy newDefinition) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new IllegalArgumentException(
                    "Unknown retention policy " + retentionPolicyName + " on " + databaseName);
        }

        retentionPolicyManager.alterRetentionPolicy(retentionPolicyName, databaseName,
                newDefinition);
    }

    /**
     * Drops the specified retention policy.
     *
     * @param retentionPolicyName the retention policy to drop
     * @param databaseName        the database the retention policy is defined on
     *
     * @throws IllegalArgumentException if {@code retentionPolicy} or {@code databaseName} are
     *                                  {@code null}
     * @throws IllegalArgumentException if either the retention policy or the database do not exist
     */
    public void dropRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new IllegalArgumentException(
                    "Unknown retention policy " + retentionPolicyName + " on " + databaseName);
        }

        retentionPolicyManager.dropRetentionPolicy(retentionPolicyName, databaseName);
    }

    /**
     * Alias for {@link #write(String, Collection)} for a single point.
     *
     * @param databaseName the database to write to, not {@code null}
     * @param data         the data to write, not {@code null}
     *
     * @throws AnnotationProcessingException if the data object is not correctly annotated
     * @throws IllegalArgumentException      if the input data is {@code null}
     * @throws IllegalArgumentException      if the database does not exist
     * @see #writePoint(String, String, Point)
     */
    public void write(String databaseName, Object data) {
        if (data == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        write(databaseName, Collections.singleton(data));
    }

    /**
     * Writes the specified data to the specified database, using the default retention policy.
     * <p>
     * The data object must be annotated in order to be converted to points and written to InfluxDB.
     *
     * @param databaseName the database to write to, not {@code null}
     * @param data         the data to write
     *
     * @throws AnnotationProcessingException if the data objects are not correctly annotated
     * @throws IllegalArgumentException      if the database does not exist
     * @see #writePoints(String, String, Collection)
     */
    public void write(String databaseName, Collection<?> data) {
        if (data.isEmpty()) {
            return;
        }

        Class<?> dataClass = data.iterator().next().getClass();
        String measurementName = namingStrategy.getMeasurementName(dataClass);
        List<Point> points = data.parallelStream()
                .map(annotationBasedPointConverter::toPoint)
                .collect(Collectors.toList());
        writePoints(databaseName, measurementName, points);
    }

    /**
     * Alias for {@link #write(String, String, Collection)} for a single point.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     * @param data                the data to write, not {@code null}
     *
     * @throws AnnotationProcessingException if the data object is not correctly annotated
     * @throws IllegalArgumentException      if the database does not exist
     * @throws IllegalArgumentException      if the retention policy does not exist
     * @see #writePoint(String, String, String, Point)
     */
    public void write(String databaseName, String retentionPolicyName, Object data) {
        if (data == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        write(databaseName, retentionPolicyName, Collections.singleton(data));
    }

    /**
     * Writes the specified data to the specified database, using the specified retention policy.
     * <p>
     * The data object must be annotated in order to be converted to points and written to InfluxDB.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     * @param data                the data to write
     *
     * @throws AnnotationProcessingException if the data objects are not correctly annotated
     * @throws IllegalArgumentException      if the database does not exist
     * @throws IllegalArgumentException      if the retention policy does not exist
     * @see #writePoints(String, String, String, Collection)
     */
    public void write(String databaseName, String retentionPolicyName, Collection<?> data) {
        if (data.isEmpty()) {
            return;
        }

        Class<?> dataClass = data.iterator().next().getClass();
        String measurementName = namingStrategy.getMeasurementName(dataClass);
        List<Point> points = data.parallelStream()
                .map(annotationBasedPointConverter::toPoint)
                .collect(Collectors.toList());
        writePoints(databaseName, measurementName, retentionPolicyName, points);
    }

    /**
     * Alias for {@link #writePoints(String, String, Collection)} for a single point.
     *
     * @param databaseName    the database to write to, not {@code null}
     * @param measurementName the measurement to write to, not {@code null}
     * @param point           the point to write
     *
     * @throws IllegalArgumentException if the database does not exist
     */
    public void writePoint(String databaseName, String measurementName, Point point) {
        writePoints(databaseName, measurementName, Collections.singleton(point));
    }

    /**
     * Writes the specified points to InfluxDB, using the default retention policy.
     *
     * @param databaseName    the database to write to, not {@code null}
     * @param measurementName the measurement to write to, not {@code null}
     * @param points          the points to write
     *
     * @throws IllegalArgumentException if the database does not exist
     */
    public void writePoints(String databaseName, String measurementName, Collection<Point> points) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        lineProtocolConverter.toLineProtocol(measurementName, points)
                .forEach(lineProtocol -> apiCaller.callApi(
                        () -> httpClient.write(databaseName, lineProtocol)));
    }

    /**
     * Alias for {@link #writePoints(String, String, String, Collection)} for a single point.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param measurementName     the measurement to write to, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     * @param point               the point to write
     *
     * @throws IllegalArgumentException if the database or retention policy does not exist
     */
    public void writePoint(String databaseName, String measurementName, String retentionPolicyName,
            Point point) {
        writePoints(databaseName, measurementName, retentionPolicyName,
                Collections.singleton(point));
    }

    /**
     * Writes the specified points to InfluxDB.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param measurementName     the measurement to write to, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     * @param points              the points to write
     *
     * @throws IllegalArgumentException if the database or retention policy does not exist
     */
    public void writePoints(String databaseName, String measurementName, String retentionPolicyName,
            Collection<Point> points) {
        if (!databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new IllegalArgumentException("Unknown retention policy " + retentionPolicyName);
        }

        lineProtocolConverter.toLineProtocol(measurementName, points)
                .forEach(lineProtocol -> apiCaller.callApi(
                        () -> httpClient.write(databaseName, retentionPolicyName, lineProtocol)));
    }

    /**
     * Retrieves all points for the specified measurement.
     *
     * @param databaseName    the database where the measurement is found, not {@code null}
     * @param measurementName the measurement to query, not {@code null}
     *
     * @return the retrieved points, or an empty list if no results or measurement does not exist
     *
     * @throws IllegalArgumentException if the database or measurement name is {@code null}
     * @throws IllegalArgumentException if the database does not exist
     */
    public List<Point> getAllPoints(String databaseName, String measurementName) {
        if (measurementName == null) {
            throw new IllegalArgumentException("Measurement name cannot be blank");
        }

        if (!databaseManager.databaseExists(databaseName)) {
            throw new IllegalArgumentException("Unknown database " + databaseName);
        }

        String query = "SELECT * FROM \"" + databaseName + "\"..\"" + measurementName + '"';
        Measurement callResult = apiCaller.callApi(() -> httpClient.query(query));
        return callResult == null ? Collections.emptyList() : callResult.getPoints();
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
            DatabaseManager databaseManager = new DatabaseManager(httpClient);
            RetentionPolicyManager retentionPolicyManager = new RetentionPolicyManager(httpClient);
            return new JFluxClient(httpClient, databaseManager, retentionPolicyManager);
        }
    }
}
