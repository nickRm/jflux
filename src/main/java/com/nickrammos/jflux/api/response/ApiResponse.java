package com.nickrammos.jflux.api.response;

import java.util.ArrayList;
import java.util.List;

/**
 * A response from the InfluxDB API, acts as a wrapper for the (possibly multiple) results returned.
 * <p>
 * A response will have more than one result when a query with more than one statement is executed.
 * For instance, the following query will return a response with two results:
 * <p><blockquote><pre>{@code
 * SELECT * FROM measurement_1; SELECT * FROM measurement_2
 * }</pre></blockquote>
 */
public final class ApiResponse {

	private final List<QueryResult> results;

	/**
	 * Instances of this class can only be created using {@link Builder}.
	 *
	 * @param builder used to construct this instance
	 */
	private ApiResponse(Builder builder) {
		this.results = builder.results;
	}

	public List<QueryResult> getResults() {
		return new ArrayList<>(results);
	}

	@Override
	public String toString() {
		return "ApiResponse{" + "results=" + results + '}';
	}

	/**
	 * Creates instances of {@link ApiResponse}.
	 */
	public static final class Builder {

		private List<QueryResult> results;

		public Builder results(List<QueryResult> results) {
			this.results = results;
			return this;
		}

		public ApiResponse build() {
			return new ApiResponse(this);
		}
	}
}
