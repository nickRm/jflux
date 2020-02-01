package com.github.nickrm.jflux.exception;

/**
 * Thrown to indicate that a retention policy cannot be created because a retention policy with the
 * same name already exists on the database.
 *
 * @since 1.0.0
 */
public final class RetentionPolicyAlreadyExistsException extends RuntimeException {

    /**
     * Creates a new instance setting the message.
     *
     * @param retentionPolicyName the name of the retention policy which already exists
     * @param databaseName        the name of the database where the retention policy is defined
     */
    public RetentionPolicyAlreadyExistsException(String retentionPolicyName, String databaseName) {
        super("Retention policy " + retentionPolicyName + " already exists on database "
                + databaseName);
    }
}
