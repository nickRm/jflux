package com.nickrammos.jflux.api.response;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of data that share a retention policy, measurement, and tag set.
 *
 * @see Builder
 */
public final class InfluxSeries {

	private final String name;
	private final List<String> columns;
	private final List<List<Object>> values;

	/**
	 * Instances of this class can only be constructed using {@link Builder}.
	 *
	 * @param builder used to construct the instance
	 */
	private InfluxSeries(Builder builder) {
		name = builder.name;
		columns = builder.columns;
		values = builder.values;
	}

	/**
	 * Gets the name of this series.
	 *
	 * @return the series name, or {@code null} if not available
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the names of the columns in this series.
	 * <p>
	 * The column names match the values in {@link #getValues()}.
	 *
	 * @return the column names, or an empty list if no results
	 */
	public List<String> getColumns() {
		return new ArrayList<>(columns);
	}

	/**
	 * Gets the values in this series as rows of cells.
	 * <p>
	 * The values match the column names in {@link #getColumns()}.
	 *
	 * @return the series values, or an empty list if no results
	 */
	public List<List<Object>> getValues() {
		return new ArrayList<>(values);
	}

	@Override
	public String toString() {
		return "InfluxSeries{" + "name='" + name + '\'' + ", columns=" + columns + ", values="
				+ values + '}';
	}

	/**
	 * Creates instances of {@link InfluxSeries}.
	 */
	public static final class Builder {

		private String name;
		private List<String> columns;
		private List<List<Object>> values;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder columns(List<String> columns) {
			this.columns = columns;
			return this;
		}

		public Builder values(List<List<Object>> values) {
			this.values = values;
			return this;
		}

		public InfluxSeries build() {
			return new InfluxSeries(this);
		}
	}
}
