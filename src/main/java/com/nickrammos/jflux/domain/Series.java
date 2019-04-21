package com.nickrammos.jflux.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of data that share a retention policy, measurement, and tag set.
 *
 * @see Builder
 */
public final class Series {

	private final String name;
	private final Set<String> tags;
	private final List<Point> points;

	/**
	 * Instances of this class can only be constructed using {@link Builder}.
	 *
	 * @param builder used to construct the instance
	 */
	private Series(Builder builder) {
		name = builder.name;
		tags = builder.tags;
		points = builder.points;
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
	 * Gets the tags for this series.
	 *
	 * @return the series tags, or an empty set if none
	 */
	public Set<String> getTags() {
		return new HashSet<>(tags);
	}

	/**
	 * Gets the points in this series.
	 *
	 * @return the serie's points, or an empty list if none
	 */
	public List<Point> getPoints() {
		return new ArrayList<>(points);
	}

	@Override
	public String toString() {
		return "Series{" + "name='" + name + '\'' + ", tags=" + tags + ", points=" + points + '}';
	}

	/**
	 * Creates instances of {@link Series}.
	 */
	public static final class Builder {

		private String name;
		private Set<String> tags;
		private List<Point> points;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder tags(Set<String> tags) {
			this.tags = tags;
			return this;
		}

		public Builder points(List<Point> points) {
			this.points = points;
			return this;
		}

		public Series build() {
			return new Series(this);
		}
	}
}
