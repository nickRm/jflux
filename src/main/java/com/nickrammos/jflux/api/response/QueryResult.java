package com.nickrammos.jflux.api.response;

import java.util.ArrayList;
import java.util.List;

import com.nickrammos.jflux.domain.Series;

/**
 * An InfluxQL result, acts as a wrapper for the (possibly multiple) series returned.
 * <p>
 * A result will contain more than one series if more than one measurement were queried. For
 * instance, the following query will return a result with two series:
 * <p><blockquote><pre>{@code
 * SELECT * FROM measurement_1, measurement_2
 * }</pre></blockquote>
 *
 * @see Builder
 */
public final class QueryResult {

	private final int statementId;
	private final List<Series> series;

	/**
	 * Instances of this class can only be constructed using {@link Builder}.
	 *
	 * @param builder used to construct the instance
	 */
	private QueryResult(Builder builder) {
		statementId = builder.statementId;
		series = builder.series;
	}

	/**
	 * Gets the ID of the statement that this result corresponds to.
	 * <p>
	 * The ID is a zero-based index used to differentiate the multiple results for multi-statement
	 * queries. For single statement queries this will just be zero and can be ignored.
	 *
	 * @return this result's statement ID
	 */
	public int getStatementId() {
		return statementId;
	}

	/**
	 * Gets the series contained in this result.
	 *
	 * @return this result's series, or an empty list if none are available
	 */
	public List<Series> getSeries() {
		return new ArrayList<>(series);
	}

	@Override
	public String toString() {
		return "QueryResult{" + "statementId=" + statementId + ", series=" + series + '}';
	}

	/**
	 * Creates instances of {@link QueryResult}.
	 */
	public static final class Builder {

		private int statementId;
		private List<Series> series;

		public Builder statementId(int statementId) {
			this.statementId = statementId;
			return this;
		}

		public Builder series(List<Series> series) {
			this.series = series;
			return this;
		}

		public QueryResult build() {
			return new QueryResult(this);
		}
	}
}
