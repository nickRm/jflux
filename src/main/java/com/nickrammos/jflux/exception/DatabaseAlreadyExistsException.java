package com.nickrammos.jflux.exception;

/**
 * Thrown to indicate that a database cannot be created because a database with the specified name
 * already exists.
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
