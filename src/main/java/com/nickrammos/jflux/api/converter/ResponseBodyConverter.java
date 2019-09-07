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

package com.nickrammos.jflux.api.converter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.domain.Point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;

/**
 * Extracts the results from a {@link ResponseBody} of a call to the InfluxDB API.
 */
final class ResponseBodyConverter implements Converter<ResponseBody, List<QueryResult>> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ResponseBodyConverter.class);

    private final ObjectMapper objectMapper;

    ResponseBodyConverter() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public List<QueryResult> convert(ResponseBody responseBody) throws IOException {
        String content = responseBody.string();
        if (content.endsWith("\n")) {
            content = content.substring(0, content.length() - 1);
        }
        LOGGER.debug("Converting response body: {}", content);
        ResponseDto responseDto = objectMapper.readValue(content, ResponseDto.class);
        return responseFromDto(responseDto);
    }

    private List<QueryResult> responseFromDto(ResponseDto responseDto) {
        List<QueryResult> results = new LinkedList<>();
        if (responseDto.results != null) {
            for (ResultDto resultDto : responseDto.results) {
                results.add(resultFromDto(resultDto));
            }
        }
        return results;
    }

    private QueryResult resultFromDto(ResultDto resultDto) {
        List<Measurement> measurements = new LinkedList<>();
        if (resultDto.series != null) {
            for (SeriesDto seriesDto : resultDto.series) {
                measurements.add(seriesFromDto(seriesDto));
            }
        }
        return new QueryResult.Builder().statementId(resultDto.statementId)
                .error(resultDto.error)
                .series(measurements)
                .build();
    }

    private Measurement seriesFromDto(SeriesDto seriesDto) {
        List<Point> points = pointsFromSeriesDto(seriesDto);

        return new Measurement.Builder().name(seriesDto.name)
                .points(points)
                .build();
    }

    private List<Point> pointsFromSeriesDto(SeriesDto seriesDto) {
        List<Point> points = new LinkedList<>();
        if (seriesDto.columns != null && seriesDto.values != null) {
            for (Object[] row : seriesDto.values) {
                Point point = pointFromRow(seriesDto.columns, row);
                points.add(point);
            }
        }
        return points;
    }

    private Point pointFromRow(String[] columns, Object[] row) {
        Instant timestamp = null;
        int startIndex = 0;
        if (columns[0].equals("time")) {
            timestamp = Instant.parse(row[0].toString());
            startIndex = 1;
        }

        Map<String, String> tags = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        for (int i = startIndex; i < columns.length; i++) {
            String columnName = columns[i];
            Object value = row[i];
            if (value instanceof Number || value instanceof Boolean) {
                fields.put(columnName, value);
            }
            else {
                tags.put(columnName, String.valueOf(value));
            }
        }

        return new Point.Builder().timestamp(timestamp).tags(tags).fields(fields).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class ResponseDto {

        @JsonProperty
        private ResultDto[] results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class ResultDto {

        @JsonProperty("statement_id")
        private int statementId;

        @JsonProperty
        private String error;

        @JsonProperty
        private SeriesDto[] series;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class SeriesDto {

        @JsonProperty
        private String name;

        @JsonProperty
        private String[] columns;

        @JsonProperty
        private Object[][] values;
    }
}
