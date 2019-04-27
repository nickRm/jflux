/*
 * Copyright 2019 Nick Rammos
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nickrammos.jflux.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of fields within a series.
 */
public final class Point {

	private final Instant timestamp;
	private final Map<String, String> tags;
	private final Map<String, Object> fields;

	/**
	 * Instances of this class can only be created using {@link Builder}.
	 *
	 * @param builder the builder used to construct this instance
	 */
	private Point(Builder builder) {
		timestamp = builder.timestamp;
		tags = builder.tags;
		fields = builder.fields;
	}

	/**
	 * Gets the timestamp of this point.
	 *
	 * @return this point's timestamp
	 */
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the tags for this point.
	 * <p>
	 * The tags are returned as a map of tag keys to their respective values.
	 *
	 * @return this point's tags, or an empty map if none set
	 */
	public Map<String, String> getTags() {
		return new HashMap<>(tags);
	}

	/**
	 * Gets the fields contained in this point.
	 * <p>
	 * The fields are returned as a map of field keys to their respective values.
	 *
	 * @return this point's fields, or an empty map if none set
	 */
	public Map<String, Object> getFields() {
		return new HashMap<>(fields);
	}

	@Override
	public String toString() {
		return "Point{" + "timestamp=" + timestamp + ", tags=" + tags + ", fields=" + fields + '}';
	}

	/**
	 * Used to create instances of {@link Point}.
	 */
	public static final class Builder {

		private Instant timestamp;
		private Map<String, String> tags = Collections.emptyMap();
		private Map<String, Object> fields = Collections.emptyMap();

		public Builder timestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder tags(Map<String, String> tags) {
			this.tags = tags;
			return this;
		}

		public Builder fields(Map<String, Object> fields) {
			this.fields = fields;
			return this;
		}

		public Point build() {
			return new Point(this);
		}
	}
}
