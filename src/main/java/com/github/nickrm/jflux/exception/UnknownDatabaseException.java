package com.github.nickrm.jflux.exception;

/**
 * Throw to indicate that an operation cannot be performed on a database because the database does
 * not exist.
 */
public final class UnknownDatabaseException extends RuntimeException {

    /**
     * Creates a new instance setting the name of the unknown database.
     *
     * @param databaseName the database name
     */
    public UnknownDatabaseException(String databaseName) {
        super("Unknown database " + databaseName);
    }
}
