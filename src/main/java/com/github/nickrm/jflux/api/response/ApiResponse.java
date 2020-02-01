/*
 * Copyright 2019 Nick Rammos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nickrm.jflux.api.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    private final ResponseMetadata metadata;
    private final int statusCode;
    private final String errorMessage;
    private final List<QueryResult> results;

    /**
     * Instances of this class can only be created using {@link Builder}.
     *
     * @param builder used to construct this instance
     */
    private ApiResponse(Builder builder) {
        this.metadata = builder.metadata;
        this.statusCode = builder.statusCode;
        this.errorMessage = builder.errorMessage;
        this.results = builder.results;
    }

    /**
     * Gets the metadata for this response.
     *
     * @return the response metadata
     */
    public ResponseMetadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the status code of this response.
     *
     * @return the response status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets a value indicating whether any of the results in this response returned an error.
     *
     * @return {@code true} if the response contains any errors, {@code false} otherwise
     */
    public boolean hasError() {
        return getErrorMessage() != null;
    }

    /**
     * Gets the error message for this response, if any.
     * <p>
     * If this response contains multiple errors, this method returns the first one.
     *
     * @return the response error message, or {@code null} if no error
     */
    public String getErrorMessage() {
        return errorMessage != null ? errorMessage : results.stream()
                .map(QueryResult::getError)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public List<QueryResult> getResults() {
        return new ArrayList<>(results);
    }

    @Override
    public String toString() {
        return "ApiResponse{" + ", statusCode=" + statusCode + ", " + "errorMessage='"
                + errorMessage + '\'' + ", results=" + results + '}';
    }

    /**
     * Creates instances of {@link ApiResponse}.
     */
    public static final class Builder {

        private ResponseMetadata metadata;
        private int statusCode;
        private String errorMessage;
        private List<QueryResult> results = Collections.emptyList();

        /**
         * Sets the metadata for this response.
         *
         * @param metadata the response metadata
         *
         * @return this builder
         */
        public Builder metadata(ResponseMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Sets the status code for this response.
         *
         * @param statusCode the response status code
         *
         * @return this builder
         */
        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Sets the error message for this response.
         *
         * @param errorMessage the response error message, or {@code null} for no error
         *
         * @return this builder
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Sets the query results for this response.
         *
         * @param results the response query results
         *
         * @return this builder
         */
        public Builder results(List<QueryResult> results) {
            this.results = results;
            return this;
        }

        /**
         * Builds a new {@link ApiResponse} instance with the values set in this builder.
         *
         * @return the newly built instance
         */
        public ApiResponse build() {
            return new ApiResponse(this);
        }
    }
}
