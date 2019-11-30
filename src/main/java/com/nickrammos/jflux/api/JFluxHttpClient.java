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

package com.nickrammos.jflux.api;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.nickrammos.jflux.api.converter.ApiResponseConverter;
import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.exception.IllegalStatementException;
import com.nickrammos.jflux.exception.InfluxClientException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Makes calls to the InfluxDB HTTP API.
 *
 * @see Builder
 */
public final class JFluxHttpClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JFluxHttpClient.class);

    private static final Pattern MULTI_SERIES_PATTERN = Pattern.compile("SELECT .* FROM .+,.+");
    private static final Pattern SELECT_INTO_PATTERN = Pattern.compile("SELECT .* INTO .* FROM "
            + ".*");

    private static final MediaType LINE_PROTOCOL_MEDIA_TYPE =
            MediaType.get("application/octet-stream");

    private final InfluxHttpService service;
    private final ApiResponseConverter responseConverter;
    private String hostUrl;

    /**
     * Initializes a new instance setting the service to be used for calls to the API.
     *
     * @param service           the API service
     * @param responseConverter used to convert API responses
     */
    private JFluxHttpClient(InfluxHttpService service, ApiResponseConverter responseConverter) {
        this.service = service;
        this.responseConverter = responseConverter;
    }

    /**
     * Gets the URL of the InfluxDB instance this client is pointed to.
     *
     * @return the InfluxDB host URL
     */
    public String getHostUrl() {
        return hostUrl;
    }

    /**
     * Tests the connection to the InfluxDB API and returns the result.
     *
     * @return metadata about the InfluxDB instance
     *
     * @throws IOException if the instance is not reachable
     */
    public ResponseMetadata ping() throws IOException {
        Call<ResponseBody> call = service.ping();
        Response<ResponseBody> response = call.execute();
        return responseConverter.convert(response).getMetadata();
    }

    /**
     * Executes a query and returns the result.
     * <p>
     * This method expects that the query will produce a single result, i.e. is a single statement,
     * querying a single measurement. The following example query can be executed with this method:
     * <p><blockquote><pre>{@code
     * SELECT * FROM measurement_1
     * }</pre></blockquote><p>
     * The following queries will result in an exception:
     * <p><blockquote><pre>{@code
     * SELECT * FROM measurement_1, measurement_2
     * SELECT * FROM measurement_1; SELECT * FROM measurement_2
     * }</pre></blockquote><p>
     * For multi-series or batch queries, see {@link #queryMultipleSeries(String)} and
     * {@link #batchQuery(String)} respectively instead.
     *
     * @param query the query to execute
     *
     * @return the query result, or {@code null} if no results
     *
     * @throws IllegalStatementException if the query format is invalid
     * @throws IOException               if query execution fails
     * @see #queryMultipleSeries(String)
     * @see #batchQuery(String)
     */
    public Measurement query(String query) throws IOException {
        if (MULTI_SERIES_PATTERN.matcher(query).matches()) {
            throw new IllegalStatementException("Query cannot span multiple measurements");
        }

        List<Measurement> measurements = queryMultipleSeries(query).getResults();
        return measurements.isEmpty() ? null : measurements.get(0);
    }

    /**
     * Executes a query and returns the result.
     * <p>
     * This method can be used to query across multiple series. Queries such as the following can
     * be used:
     * <p><blockquote><pre>{@code
     * SELECT * FROM measurement_1, measurement_2
     * }</pre></blockquote><p>
     * Note that while single serie queries are possible with this method, the responsibility of
     * unwrapping the result falls then on the caller. For a more convenient way of executing
     * single serie queries see {@link #query(String)}.
     *
     * @param query the query to execute
     *
     * @return the query result
     *
     * @throws IllegalStatementException if the query format is invalid
     * @throws IOException               if query execution fails
     * @see #query(String)
     * @see #batchQuery(String)
     */
    public QueryResult queryMultipleSeries(String query) throws IOException {
        if (query.contains(";")) {
            throw new IllegalStatementException("Query cannot contain multiple statements");
        }

        return batchQuery(query).getResults().get(0);
    }

    /**
     * Executes a query and returns the result.
     * <p>
     * This method can be used to execute multiple queries at once, spanning one or more
     * measurements. Queries such as the following can be executed:
     * <p><blockquote><pre>{@code
     * SELECT * FROM measurement_1; SELECT * FROM measurement_2, measurement_3;
     * }</pre></blockquote><p>
     * Note that while single statement and/or single serie queries are also possible with this
     * method, the responsibility of unwrapping the result falls then on the caller. For more
     * convenient ways of executing single statement and single serie queries see
     * {@link #query(String)} and {@link #queryMultipleSeries(String)}.
     *
     * @param query the query to execute
     *
     * @return the query result
     *
     * @throws IllegalStatementException if the query format is invalid
     * @throws IOException               if query execution fails
     * @see #query(String)
     * @see #queryMultipleSeries(String)
     */
    public ApiResponse batchQuery(String query) throws IOException {
        if (SELECT_INTO_PATTERN.matcher(query).matches()) {
            throw new IllegalStatementException("Cannot execute 'SELECT INTO' as query");
        }

        ApiResponse response = callApi(service::query, query);
        LOGGER.debug("Received {}", response);
        return response;
    }

    /**
     * Executes DDL statements, such as {@code CREATE} or {@code ALTER}.
     *
     * @param statement the statement to execute
     *
     * @throws IOException if execution fails
     */
    public void execute(String statement) throws IOException {
        callApi(service::alter, statement);
    }

    /**
     * Writes points into the default retention policy.
     * <p>
     * Points to be written are specified using InfluxDB's
     * <a href="https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_tutorial/">
     * line protocol</a> format. Multiple points can be written by separating them with a {@code \n}
     * character.
     *
     * @param database     the database to write to
     * @param lineProtocol the point(s) to write
     *
     * @return the API's response
     *
     * @throws IOException           if InfluxDB cannot be reached
     * @throws InfluxClientException if the points are not in the correct format
     */
    public ApiResponse write(String database, String lineProtocol) throws IOException {
        LOGGER.debug("Writing line '{}' to {}", lineProtocol, database);
        RequestBody requestBody = RequestBody.create(LINE_PROTOCOL_MEDIA_TYPE, lineProtocol);
        return callApi(() -> service.write(database, requestBody));
    }

    /**
     * Writes points into the specified retention policy.
     * <p>
     * Points to be written are specified using InfluxDB's
     * <a href="https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_tutorial/">
     * line protocol</a> format. Multiple points can be written by separating them with a {@code \n}
     * character.
     *
     * @param database        the database to write to
     * @param retentionPolicy the retention policy to use
     * @param lineProtocol    the point(s) to write
     *
     * @return the API's response
     *
     * @throws IOException           if InfluxDB cannot be reached
     * @throws InfluxClientException if the points are not in the correct format
     */
    public ApiResponse write(String database, String retentionPolicy, String lineProtocol)
            throws IOException {
        LOGGER.debug("Writing line '{}' to {}.{}", lineProtocol, database, retentionPolicy);
        RequestBody requestBody = RequestBody.create(LINE_PROTOCOL_MEDIA_TYPE, lineProtocol);
        return callApi(() -> service.write(database, retentionPolicy, requestBody));
    }

    private ApiResponse callApi(Function<String, Call<ResponseBody>> apiMethod, String statement)
            throws IOException {
        LOGGER.debug("Executing statement '{}'", statement);
        return callApi(() -> apiMethod.apply(statement));
    }

    private ApiResponse callApi(Supplier<Call<ResponseBody>> apiMethod) throws IOException {
        Call<ResponseBody> call = apiMethod.get();
        Response<ResponseBody> responseWrapper = call.execute();
        LOGGER.debug("Received response: {}", responseWrapper);
        ApiResponse response = responseConverter.convert(responseWrapper);
        if (response.hasError()) {
            throw new InfluxClientException(response.getErrorMessage());
        }
        else {
            return response;
        }
    }

    @Override
    public void close() {
        // Nothing to close for now.
    }

    /**
     * Used to construct {@link JFluxHttpClient} instances.
     */
    public static final class Builder {

        private String host;

        /**
         * Initializes a new builder instance, setting the InfluxDB host URL.
         *
         * @param host the InfluxDB host URL, e.g. {@code http://localhost:8086}
         */
        public Builder(String host) {
            this.host = host;
        }

        /**
         * Constructs a new {@link JFluxHttpClient} instance from this builder's configuration.
         *
         * @return the new client instance
         */
        public JFluxHttpClient build() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(host)
                    .build();
            InfluxHttpService service = retrofit.create(InfluxHttpService.class);
            JFluxHttpClient client = new JFluxHttpClient(service, new ApiResponseConverter());
            client.hostUrl = host;
            return client;
        }
    }
}
