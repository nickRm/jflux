package com.nickrammos.jflux.api.response;

import java.util.ArrayList;
import java.util.List;

/**
 * A response from the InfluxDB API, acts as a wrapper for the (possibly multiple) results returned.
 *
 * <p>
 * A response will have more than one result when a query with more than one statement is executed.
 * For instance, the following query will return a response with two results:
 * <pre>
 * {@code SELECT * FROM measurement_1; SELECT * FROM measurement_2}
 * </pre>
 */
public final class InfluxResponse {

	private final List<InfluxResult> results;

	/**
	 * Instances of this class can only be created using {@link Builder}.
	 *
	 * @param builder used to construct this instance
	 */
	private InfluxResponse(Builder builder) {
		this.results = builder.results;
	}

	public List<InfluxResult> getResults() {
		return new ArrayList<>(results);
	}

	@Override
	public String toString() {
		return "InfluxResponse{" + "results=" + results + '}';
	}

	/**
	 * Creates instances of {@link InfluxResponse}.
	 */
	public static final class Builder {

		private List<InfluxResult> results;

		public Builder results(List<InfluxResult> results) {
			this.results = results;
			return this;
		}

		public InfluxResponse build() {
			return new InfluxResponse(this);
		}
	}
}
