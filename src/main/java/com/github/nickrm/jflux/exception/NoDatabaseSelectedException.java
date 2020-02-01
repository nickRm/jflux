package com.github.nickrm.jflux.exception;

import com.github.nickrm.jflux.JFluxClient;

/**
 * Thrown to indicate that an operation that requires a database to be preselected has been called,
 * but a database has not been selected.
 * <p>
 * This is normally fixable by calling {@link JFluxClient#useDatabase(String)} with the desired
 * database argument, before calling the operation which caused the exception.
 */
public final class NoDatabaseSelectedException extends IllegalStateException {

    /**
     * Creates a new instance setting a default detail message.
     */
    public NoDatabaseSelectedException() {
        super("No database selected, JFluxClient.useDatabase(String) must be called first");
    }
}
