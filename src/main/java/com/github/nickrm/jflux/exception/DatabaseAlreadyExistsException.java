package com.github.nickrm.jflux.exception;

/**
 * Thrown to indicate that a database cannot be created because a database with the specified name
 * already exists.
 *
 * @since 1.0.0
 */
public final class DatabaseAlreadyExistsException extends RuntimeException {

    /**
     * Creates a new instance setting the name of the database.
     *
     * @param databaseName the database name
     */
    public DatabaseAlreadyExistsException(String databaseName) {
        super("Database " + databaseName + " already exists");
    }
}
