package com.github.nickrm.jflux;

import java.util.List;
import java.util.stream.Collectors;

import com.github.nickrm.jflux.api.JFluxHttpClient;
import com.github.nickrm.jflux.domain.Measurement;
import com.github.nickrm.jflux.domain.RetentionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles retention policy management functionality.
 */
final class RetentionPolicyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetentionPolicyManager.class);

    private final JFluxHttpClient httpClient;
    private final ApiCaller apiCaller;

    RetentionPolicyManager(JFluxHttpClient httpClient) {
        this.httpClient = httpClient;
        apiCaller = new ApiCaller();
    }

    /**
     * Gets all the retention policies defined on the specified database.
     *
     * @param databaseName name of the database to check, not {@code null}
     *
     * @return the database's retention policies
     *
     * @throws IllegalArgumentException if {@code databaseName} is {@code null}
     */
    List<RetentionPolicy> getRetentionPolicies(String databaseName) {
        if (databaseName == null) {
            throw new IllegalArgumentException("Database name cannot be null");
        }

        String query = "SHOW RETENTION POLICIES ON \"" + databaseName + "\"";
        Measurement queryResult = apiCaller.callApi(() -> httpClient.query(query));

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
     * @throws IllegalArgumentException if {@code retentionPolicyName} or {@code databaseName} are
     *                                  {@code null}
     */
    RetentionPolicy getRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (retentionPolicyName == null) {
            throw new IllegalArgumentException("Retention policy cannot be null");
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
     * @throws NullPointerException if the retention policy or database names are null
     */
    boolean retentionPolicyExists(String retentionPolicyName, String databaseName) {
        return getRetentionPolicy(retentionPolicyName, databaseName) != null;
    }

    /**
     * Creates a new retention policy on the specified database.
     *
     * @param retentionPolicy the retention policy to create
     * @param databaseName    the database to create the retention policy on
     *
     * @throws IllegalArgumentException if {@code retentionPolicy} or {@code databaseName} is {@code
     *                                  null}
     */
    void createRetentionPolicy(RetentionPolicy retentionPolicy, String databaseName) {
        if (retentionPolicy == null) {
            throw new IllegalArgumentException("Retention policy cannot be null");
        }

        if (databaseName == null) {
            throw new IllegalArgumentException("Database name cannot be null");
        }

        DurationConverter durationConverter = new DurationConverter();
        String statement = "CREATE RETENTION POLICY \"" + retentionPolicy.getName() + '"'
                + " ON \"" + databaseName + '"'
                + " DURATION " + durationConverter.toLiteral(retentionPolicy.getDuration())
                + " REPLICATION " + retentionPolicy.getReplication()
                + " SHARD DURATION " + durationConverter.toLiteral(
                retentionPolicy.getShardDuration())
                + (retentionPolicy.isDefault() ? " DEFAULT" : "");
        apiCaller.callApi(() -> httpClient.execute(statement));
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
     * @throws IllegalArgumentException if any of the arguments are {@code null}
     */
    void alterRetentionPolicy(String retentionPolicyName, String databaseName,
            RetentionPolicy newDefinition) {
        if (retentionPolicyName == null) {
            throw new IllegalArgumentException("Retention policy name cannot be null");
        }

        if (databaseName == null) {
            throw new IllegalArgumentException("Database name cannot be null");
        }

        if (newDefinition == null) {
            throw new IllegalArgumentException("Retention policy definition cannot be null");
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
        apiCaller.callApi(() -> httpClient.execute(statement));
        LOGGER.info("Updated '{}'.'{}' to {}", databaseName, retentionPolicyName, newDefinition);
    }

    /**
     * Drops the specified retention policy.
     *
     * @param retentionPolicyName the retention policy to drop
     * @param databaseName        the database the retention policy is defined on
     *
     * @throws IllegalArgumentException if {@code retentionPolicy} or {@code databaseName} are
     *                                  {@code null}
     */
    void dropRetentionPolicy(String retentionPolicyName, String databaseName) {
        if (retentionPolicyName == null) {
            throw new IllegalArgumentException("Retention policy name cannot be null");
        }

        if (databaseName == null) {
            throw new IllegalArgumentException("Database name cannot be null");
        }

        String statement =
                "DROP RETENTION POLICY \"" + retentionPolicyName + "\" ON \"" + databaseName + '"';
        apiCaller.callApi(() -> httpClient.execute(statement));
        LOGGER.info("Dropped retention policy '{}' on '{}'", retentionPolicyName, databaseName);
    }
}
