package com.github.nickrm.jflux.exception;

/**
 * Thrown to indicate that an operation cannot be performed on a retention policy because the
 * retention policy does not exist.
 */
public final class UnknownRetentionPolicyException extends RuntimeException {

    /**
     * Creates a new instance setting the message.
     *
     * @param retentionPolicyName the name of the retention policy which could not be found
     * @param databaseName        the database on which the retention policy was not found
     */
    public UnknownRetentionPolicyException(String retentionPolicyName, String databaseName) {
        super("Unknown retention policy " + retentionPolicyName + " on database " + databaseName);
    }
}
