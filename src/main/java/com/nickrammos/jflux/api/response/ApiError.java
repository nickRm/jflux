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

package com.nickrammos.jflux.api.response;

import java.time.Instant;

/**
 * Contains information about an error response from the InfluxDB API.
 *
 * @see Builder
 */
public final class ApiError {

	private final Instant timestamp;
	private final int statusCode;
	private final String message;

	/**
	 * Instances can only be created using {@link Builder}.
	 *
	 * @param builder used to construct this instance
	 */
	private ApiError(Builder builder) {
		timestamp = builder.timestamp;
		statusCode = builder.statusCode;
		message = builder.message;
	}

	/**
	 * Gets the status code associated with this error.
	 *
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Gets the message associated with this error.
	 *
	 * @return the error message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ApiError{" + "timestamp=" + timestamp + ", statusCode=" + statusCode + ", "
				+ "message='" + message + '\'' + '}';
	}

	/**
	 * Constructs instances of {@link ApiError}.
	 */
	public static final class Builder {

		private Instant timestamp;
		private int statusCode;
		private String message;

		public Builder(int statusCode, String message) {
			timestamp = Instant.now();
			statusCode(statusCode);
			message(message);
		}

		public Builder timestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder statusCode(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public ApiError build() {
			return new ApiError(this);
		}
	}
}
