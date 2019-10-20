package com.nickrammos.jflux.exception;

/**
 * Thrown to indicate that something went wrong when processing an annotated class or object.
 */
public class AnnotationProcessingException extends RuntimeException {

    /**
     * Constructs a new exception with the specified message.
     *
     * @param msg the exception message
     */
    AnnotationProcessingException(String msg) {
        this(msg, null);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     *
     * @param msg   the exception message
     * @param cause the cause of this exception
     */
    public AnnotationProcessingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
