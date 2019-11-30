package com.nickrammos.jflux.api.exception;

/**
 * Thrown to indicate that an InfluxQL statement cannot be executed because it has invalid format
 * and/or syntax.
 */
public final class IllegalStatementException extends RuntimeException {

    /**
     * Creates a new instance setting the message.
     *
     * @param message the exception message
     */
    public IllegalStatementException(String message) {
        super(message);
    }
}
