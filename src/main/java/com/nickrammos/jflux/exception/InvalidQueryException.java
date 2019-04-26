package com.nickrammos.jflux.exception;

/**
 * Thrown to indicate that a query was syntactically or otherwise incorrect (e.g. wrong database
 * name).
 */
public final class InvalidQueryException extends RuntimeException {

	/**
	 * Initializes a new instance setting the message.
	 *
	 * @param message the message for this exception
	 */
	public InvalidQueryException(String message) {
		super(message);
	}
}
