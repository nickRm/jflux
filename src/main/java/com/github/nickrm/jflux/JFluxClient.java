package com.github.nickrm.jflux;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.nickrm.jflux.annotation.Field;
import com.github.nickrm.jflux.annotation.Tag;
import com.github.nickrm.jflux.annotation.Timestamp;
import com.github.nickrm.jflux.annotation.exception.AnnotationProcessingException;
import com.github.nickrm.jflux.api.JFluxHttpClient;
import com.github.nickrm.jflux.api.response.ResponseMetadata;
import com.github.nickrm.jflux.domain.Measurement;
import com.github.nickrm.jflux.domain.Point;
import com.github.nickrm.jflux.domain.RetentionPolicy;
import com.github.nickrm.jflux.exception.DatabaseAlreadyExistsException;
import com.github.nickrm.jflux.exception.NoDatabaseSelectedException;
import com.github.nickrm.jflux.exception.RetentionPolicyAlreadyExistsException;
import com.github.nickrm.jflux.exception.UnknownDatabaseException;
import com.github.nickrm.jflux.exception.UnknownRetentionPolicyException;
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
     * The database to be used when calling methods without specifying the database. This needs to
     * be set to something other than {@code null} before calling those methods.
     */
    private String currentDatabase;

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
     * @throws IllegalArgumentException       if {@code databaseName} is {@code null}
     * @throws DatabaseAlreadyExistsException if the database already exists
     */
    public void createDatabase(String databaseName) {
        if (databaseExists(databaseName)) {
            throw new DatabaseAlreadyExistsException(databaseName);
        }
        databaseManager.createDatabase(databaseName);
    }

    /**
     * Drops the specified database.
     *
     * @param databaseName the database to drop, not {@code null}
     *
     * @throws IllegalArgumentException if trying to drop the internal InfluxDB database
     * @throws IllegalArgumentException if {@code databaseName} is {@code null}
     * @throws UnknownDatabaseException if the database does not exist
     */
    public void dropDatabase(String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        databaseManager.dropDatabase(databaseName);
    }

    /**
     * Selects a database to use implicitly with methods in this class.
     * <p>
     * This is convenience method to avoid having to specify the database to perform operations on
     * in every method call, similar to InfluxDB's own {@code USE DATABASE} statement.
     *
     * @param databaseName the database to use, not {@code null}
     *
     * @throws IllegalArgumentException if {@code databaseName} is {@code null}
     * @throws UnknownDatabaseException if the specified database does not exist
     */
    public void useDatabase(String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        this.currentDatabase = databaseName;
    }

    /**
     * Gets all the retention policies defined on the currently selected database.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @return the selected database's retention policies
     *
     * @throws NoDatabaseSelectedException if no database has been selected
     */
    public List<RetentionPolicy> getRetentionPolicies() {
        assertDatabaseHasBeenSelected();
        return getRetentionPolicies(currentDatabase);
    }

    /**
     * Gets all the retention policies defined on the specified database.
     *
     * @param databaseName name of the database to check, not {@code null}
     *
     * @return the database's retention policies
     *
     * @throws UnknownDatabaseException if the database does not exist
     */
    public List<RetentionPolicy> getRetentionPolicies(String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        return retentionPolicyManager.getRetentionPolicies(databaseName);
    }

    /**
     * Gets the definition of the specified retention policy.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param retentionPolicyName the retention policy to get
     *
     * @return the retention policy definition, or {@code null} if not found
     *
     * @throws IllegalArgumentException    if {@code retentionPolicyName} is {@code null}
     * @throws NoDatabaseSelectedException if no database has been selected
     */
    public RetentionPolicy getRetentionPolicy(String retentionPolicyName) {
        assertDatabaseHasBeenSelected();
        return getRetentionPolicy(retentionPolicyName, currentDatabase);
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
     * @throws UnknownDatabaseException if the database does not exist
     */
    public RetentionPolicy getRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        return retentionPolicyManager.getRetentionPolicy(retentionPolicyName, databaseName);
    }

    /**
     * Returns a value indicating whether the specified retention policy exists on the selected
     * database.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param retentionPolicyName the retention policy to check
     *
     * @return {@code true} if the retention policy exists, {@code false} otherwise
     *
     * @throws NoDatabaseSelectedException if no database has been selected
     */
    public boolean retentionPolicyExists(String retentionPolicyName) {
        assertDatabaseHasBeenSelected();
        return retentionPolicyExists(retentionPolicyName, currentDatabase);
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
     * @throws UnknownDatabaseException if the database does not exist
     */
    public boolean retentionPolicyExists(String retentionPolicyName, String databaseName) {
        if (retentionPolicyName == null) {
            throw new IllegalArgumentException("Retention policy name cannot be null");
        }

        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        return retentionPolicyManager.retentionPolicyExists(retentionPolicyName, databaseName);
    }

    /**
     * Creates a new retention policy on the selected database.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param retentionPolicy the retention policy to create
     *
     * @throws NoDatabaseSelectedException           if no database has been selected
     * @throws IllegalArgumentException              if {@code retentionPolicy} is {@code null}
     * @throws RetentionPolicyAlreadyExistsException if the retention policy already exists
     */
    public void createRetentionPolicy(RetentionPolicy retentionPolicy) {
        assertDatabaseHasBeenSelected();
        createRetentionPolicy(retentionPolicy, currentDatabase);
    }

    /**
     * Creates a new retention policy on the specified database.
     *
     * @param retentionPolicy the retention policy to create
     * @param databaseName    the database to create the retention policy on
     *
     * @throws IllegalArgumentException              if {@code retentionPolicy} or {@code
     *                                               databaseName} is {@code null}
     * @throws UnknownDatabaseException              if the database does not exist
     * @throws RetentionPolicyAlreadyExistsException if the retention policy already exists
     */
    public void createRetentionPolicy(RetentionPolicy retentionPolicy, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }

        if (retentionPolicyExists(retentionPolicy.getName(), databaseName)) {
            throw new RetentionPolicyAlreadyExistsException(retentionPolicy.getName(),
                    databaseName);
        }

        retentionPolicyManager.createRetentionPolicy(retentionPolicy, databaseName);
    }

    /**
     * Alters the specified retention policy.
     * <p>
     * Note that the name of the retention policy cannot be altered and thus the original name will
     * always be kept, even if the new definition specifies a different one.
     * <p>
     * Note also that a database must have been already selected with {@link #useDatabase(String)}
     * before calling this method.
     *
     * @param retentionPolicyName the retention policy to alter
     * @param newDefinition       the new definition for the retention policy
     *
     * @throws NoDatabaseSelectedException     if no database has been selected
     * @throws IllegalArgumentException        if any of the arguments are {@code null}
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void alterRetentionPolicy(String retentionPolicyName, RetentionPolicy newDefinition) {
        assertDatabaseHasBeenSelected();
        alterRetentionPolicy(retentionPolicyName, currentDatabase, newDefinition);
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
     * @throws IllegalArgumentException        if any of the arguments are {@code null}
     * @throws UnknownDatabaseException        if the database does not exist
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void alterRetentionPolicy(String retentionPolicyName, String databaseName,
            RetentionPolicy newDefinition) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new UnknownRetentionPolicyException(retentionPolicyName, databaseName);
        }

        retentionPolicyManager.alterRetentionPolicy(retentionPolicyName, databaseName,
                newDefinition);
    }

    /**
     * Drops the specified retention policy on the selected database.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param retentionPolicyName the retention policy to drop
     *
     * @throws NoDatabaseSelectedException     if no database has been selected
     * @throws IllegalArgumentException        if {@code retentionPolicy} is {@code null}
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void dropRetentionPolicy(String retentionPolicyName) {
        assertDatabaseHasBeenSelected();
        dropRetentionPolicy(retentionPolicyName, currentDatabase);
    }

    /**
     * Drops the specified retention policy.
     *
     * @param retentionPolicyName the retention policy to drop
     * @param databaseName        the database the retention policy is defined on
     *
     * @throws IllegalArgumentException        if {@code retentionPolicy} or {@code databaseName}
     *                                         are {@code null}
     * @throws UnknownDatabaseException        if the database does not exist
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void dropRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new UnknownRetentionPolicyException(retentionPolicyName, databaseName);
        }

        retentionPolicyManager.dropRetentionPolicy(retentionPolicyName, databaseName);
    }

    /**
     * Alias for {@link #write(Collection)} for a single object.
     *
     * @param data the data to write, not {@code null}
     *
     * @throws NoDatabaseSelectedException   if no database has been selected
     * @throws IllegalArgumentException      if {@code data} is {@code null}
     * @throws AnnotationProcessingException if the data object is not correctly annotated
     */
    public void write(Object data) {
        write(Collections.singleton(data));
    }

    /**
     * Alias for {@link #write(String, Collection)} for a single point.
     *
     * @param databaseName the database to write to, not {@code null}
     * @param data         the data to write, not {@code null}
     *
     * @throws AnnotationProcessingException if the data object is not correctly annotated
     * @throws IllegalArgumentException      if the input data is {@code null}
     * @throws UnknownDatabaseException      if the database does not exist
     * @see #writePoint(String, String, Point)
     */
    public void write(String databaseName, Object data) {
        if (data == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        write(databaseName, Collections.singleton(data));
    }

    /**
     * Writes the specified data using the specified retention policy.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param data the data to write
     *
     * @throws NoDatabaseSelectedException   if no database has been selected
     * @throws AnnotationProcessingException if the data objects are not correctly annotated.
     */
    public void write(Collection<?> data) {
        assertDatabaseHasBeenSelected();
        write(currentDatabase, data);
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
     * @throws UnknownDatabaseException      if the database does not exist
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
     * Alias for {@link #write(Collection, String)} for a single point.
     *
     * @param data                the data to write, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws NoDatabaseSelectedException     if no database has been selected
     * @throws IllegalArgumentException        if any of the arguments is {@code null}
     * @throws AnnotationProcessingException   if the data objects are not correctly annotated
     * @throws UnknownRetentionPolicyException if the retenion policy does not exist
     */
    public void write(Object data, String retentionPolicyName) {
        write(Collections.singleton(data), retentionPolicyName);
    }

    /**
     * Alias for {@link #write(String, Collection, String)} for a single point.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param data                the data to write, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws AnnotationProcessingException if the data object is not correctly annotated
     * @throws UnknownDatabaseException      if the database does not exist
     * @throws IllegalArgumentException      if the retention policy does not exist
     * @see #writePoint(String, String, Point, String)
     */
    public void write(String databaseName, Object data, String retentionPolicyName) {
        if (data == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        write(databaseName, Collections.singleton(data), retentionPolicyName);
    }

    /**
     * Writes the specified data using the specified retention policy.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param data                the data to write
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws NoDatabaseSelectedException     if no database has been selected
     * @throws IllegalArgumentException        if {@code retentionPolicyName} is {@code null}
     * @throws AnnotationProcessingException   if the data objects are not correctly annotated
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void write(Collection<?> data, String retentionPolicyName) {
        assertDatabaseHasBeenSelected();
        write(currentDatabase, data, retentionPolicyName);
    }

    /**
     * Writes the specified data to the specified database, using the specified retention policy.
     * <p>
     * The data object must be annotated in order to be converted to points and written to InfluxDB.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param data                the data to write
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws AnnotationProcessingException if the data objects are not correctly annotated
     * @throws UnknownDatabaseException      if the database does not exist
     * @throws IllegalArgumentException      if the retention policy does not exist
     * @see #writePoints(String, String, Collection, String)
     */
    public void write(String databaseName, Collection<?> data, String retentionPolicyName) {
        if (data.isEmpty()) {
            return;
        }

        Class<?> dataClass = data.iterator().next().getClass();
        String measurementName = namingStrategy.getMeasurementName(dataClass);
        List<Point> points = data.parallelStream()
                .map(annotationBasedPointConverter::toPoint)
                .collect(Collectors.toList());
        writePoints(databaseName, measurementName, points, retentionPolicyName);
    }

    /**
     * Alias for {@link #writePoints(String, Collection)} for a single point.
     *
     * @param measurementName the measurement to write to, not {@code null}
     * @param point           the point to write, not {@code null}
     *
     * @throws NoDatabaseSelectedException if no database has been selected
     * @throws IllegalArgumentException    if any of the arguments is {@code null}
     */
    public void writePoint(String measurementName, Point point) {
        writePoints(measurementName, Collections.singleton(point));
    }

    /**
     * Alias for {@link #writePoints(String, String, Collection)} for a single point.
     *
     * @param databaseName    the database to write to, not {@code null}
     * @param measurementName the measurement to write to, not {@code null}
     * @param point           the point to write
     *
     * @throws IllegalArgumentException if the database or measurement name is {@code null}
     * @throws UnknownDatabaseException if the database does not exist
     */
    public void writePoint(String databaseName, String measurementName, Point point) {
        writePoints(databaseName, measurementName, Collections.singleton(point));
    }

    /**
     * Alias for {@link #writePoints(String, Collection, String)} for a single point.
     *
     * @param measurementName     the measurement to write to, not {@code null}
     * @param point               the point to write, not {@code null}
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws NoDatabaseSelectedException     if no database has been selected
     * @throws IllegalArgumentException        if any of the arguments is {@code null}
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void writePoint(String measurementName, Point point, String retentionPolicyName) {
        writePoints(measurementName, Collections.singleton(point), retentionPolicyName);
    }

    /**
     * Alias for {@link #writePoints(String, String, Collection, String)} for a single point.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param measurementName     the measurement to write to, not {@code null}
     * @param point               the point to write
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws UnknownDatabaseException if the database does not exist
     * @throws IllegalArgumentException if the retention policy does not exist
     */
    public void writePoint(String databaseName, String measurementName, Point point,
            String retentionPolicyName) {
        writePoints(databaseName, measurementName, Collections.singleton(point),
                retentionPolicyName);
    }

    /**
     * Writes the specified points to InfluxDB, using the default retention policy.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param measurementName the measurement to write to, not {@code null}
     * @param points          the points to write
     *
     * @throws NoDatabaseSelectedException if no database has been selected
     * @throws IllegalArgumentException    if {@code measurementName} is {@code null}
     */
    public void writePoints(String measurementName, Collection<Point> points) {
        assertDatabaseHasBeenSelected();
        writePoints(currentDatabase, measurementName, points);
    }

    /**
     * Writes the specified points to InfluxDB, using the default retention policy.
     *
     * @param databaseName    the database to write to, not {@code null}
     * @param measurementName the measurement to write to, not {@code null}
     * @param points          the points to write
     *
     * @throws IllegalArgumentException if the database or measurement name is {@code null}
     * @throws UnknownDatabaseException if the database does not exist
     */
    public void writePoints(String databaseName, String measurementName, Collection<Point> points) {
        if (databaseName == null) {
            throw new IllegalArgumentException("Database name cannot be null");
        }

        if (measurementName == null) {
            throw new IllegalArgumentException("Measurement name cannot be null");
        }

        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }

        lineProtocolConverter.toLineProtocol(measurementName, points)
                .forEach(lineProtocol -> apiCaller.callApi(
                        () -> httpClient.write(databaseName, lineProtocol)));
    }

    /**
     * Writes the specified points to InfluxDB.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param measurementName     the measurement to write to, not {@code null}
     * @param points              the points to write
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws NoDatabaseSelectedException     if no database has been selected
     * @throws IllegalArgumentException        if any of the arguments is {@code null}
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void writePoints(String measurementName, Collection<Point> points,
            String retentionPolicyName) {
        assertDatabaseHasBeenSelected();
        writePoints(currentDatabase, measurementName, points, retentionPolicyName);
    }

    /**
     * Writes the specified points to InfluxDB.
     *
     * @param databaseName        the database to write to, not {@code null}
     * @param measurementName     the measurement to write to, not {@code null}
     * @param points              the points to write
     * @param retentionPolicyName the retention policy to write to, not {@code null}
     *
     * @throws UnknownDatabaseException        if the database does not exist
     * @throws UnknownRetentionPolicyException if the retention policy does not exist
     */
    public void writePoints(String databaseName, String measurementName, Collection<Point> points,
            String retentionPolicyName) {
        if (!databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }

        if (!retentionPolicyExists(retentionPolicyName, databaseName)) {
            throw new UnknownRetentionPolicyException(retentionPolicyName, databaseName);
        }

        lineProtocolConverter.toLineProtocol(measurementName, points)
                .forEach(lineProtocol -> apiCaller.callApi(
                        () -> httpClient.write(databaseName, retentionPolicyName, lineProtocol)));
    }

    /**
     * Alias for {@link #getAllPoints(String, Class)} using a preselected database.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param targetType the class to convert to, not {@code null}
     * @param <T>        type of the results
     *
     * @return the points converted to instances of {@code targetType}, or an empty list if no
     * results
     *
     * @throws NoDatabaseSelectedException if no database selected
     */
    public <T> List<T> getAllPoints(Class<T> targetType) {
        assertDatabaseHasBeenSelected();
        return getAllPoints(currentDatabase, targetType);
    }

    /**
     * Retrieves all points for the specified class.
     * <p>
     * The measurement name is read from the class name, or the
     * {@link com.github.nickrm.jflux.annotation.Measurement} annotation if present. The resulting
     * points are converted to instances of the class, setting the values of fields annotated with
     * {@link Timestamp}, {@link Field}, and {@link Tag}.
     *
     * @param databaseName the database where the measurement is found, not {@code null}
     * @param targetType   the class to convert the results to, not {@code null}
     * @param <T>          type of the results
     *
     * @return the points converted to instance of {@code targetType}, or an empty list if no
     * results
     *
     * @throws IllegalArgumentException if {@code databaseName} or {@code targetType} is {@code
     *                                  null}
     * @throws UnknownDatabaseException if the database cannot be found
     * @see AnnotationBasedPointConverter
     */
    public <T> List<T> getAllPoints(String databaseName, Class<T> targetType) {
        String measurementName = namingStrategy.getMeasurementName(targetType);
        return getAllPoints(databaseName, measurementName).stream()
                .map(point -> annotationBasedPointConverter.fromPoint(point, targetType))
                .collect(Collectors.toList());
    }

    /**
     * Alias for {@link #getAllPoints(String, String)} using a preselected database.
     * <p>
     * Note that a database must have been already selected with {@link #useDatabase(String)} before
     * calling this method.
     *
     * @param measurementName the measurement to query, not {@code null}
     *
     * @return the retrieved points, or an empty list if no result or measurement does not exist
     *
     * @throws NoDatabaseSelectedException if no database has been selected
     * @throws IllegalArgumentException    if the measurement name is {@code null}
     */
    public List<Point> getAllPoints(String measurementName) {
        assertDatabaseHasBeenSelected();
        return getAllPoints(currentDatabase, measurementName);
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
     * @throws UnknownDatabaseException if the database does not exist
     */
    public List<Point> getAllPoints(String databaseName, String measurementName) {
        if (measurementName == null) {
            throw new IllegalArgumentException("Measurement name cannot be blank");
        }

        if (databaseName == null) {
            throw new IllegalArgumentException("Database name cannot be null");
        }

        if (!databaseManager.databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }

        String query = "SELECT * FROM \"" + databaseName + "\"..\"" + measurementName + '"';
        Measurement callResult = apiCaller.callApi(() -> httpClient.query(query));
        return callResult == null ? Collections.emptyList() : callResult.getPoints();
    }

    /**
     * Verifies that a database has been selected and throws an exception if not.
     * <p>
     * This method is meant to help with all methods which required a preselected database.
     *
     * @throws NoDatabaseSelectedException if no database has been selected
     */
    private void assertDatabaseHasBeenSelected() {
        if (currentDatabase == null) {
            throw new NoDatabaseSelectedException();
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
            DatabaseManager databaseManager = new DatabaseManager(httpClient);
            RetentionPolicyManager retentionPolicyManager = new RetentionPolicyManager(httpClient);
            return new JFluxClient(httpClient, databaseManager, retentionPolicyManager);
        }
    }
}
