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

package com.nickrammos.jflux.api.response;

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
        return results.stream()
                .map(QueryResult::getError)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "ApiResponse{" + "results=" + results + '}';
    }

    /**
     * Creates instances of {@link ApiResponse}.
     */
    public static final class Builder {

        private List<QueryResult> results = Collections.emptyList();

        public Builder results(List<QueryResult> results) {
            this.results = results;
            return this;
        }

        public ApiResponse build() {
            return new ApiResponse(this);
        }
    }
}
