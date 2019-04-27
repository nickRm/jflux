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

package com.nickrammos.jflux.api;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.Series;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;

/**
 * Converts a {@link ResponseBody} from a call to the InfluxDB API, to an {@link ApiResponse}.
 */
final class ResponseBodyConverter implements Converter<ResponseBody, ApiResponse> {

	private static final Logger LOGGER =
			LoggerFactory.getLogger(ResponseBodyConverter.class);

	private final ObjectMapper objectMapper;

	ResponseBodyConverter() {
		objectMapper = new ObjectMapper();
	}

	@Override
	public ApiResponse convert(ResponseBody responseBody) throws IOException {
		String content = responseBody.string();
		if (content.endsWith("\n")) {
			content = content.substring(0, content.length() - 1);
		}
		LOGGER.debug("Converting response body: {}", content);
		ResponseDto responseDto = objectMapper.readValue(content, ResponseDto.class);
		return responseFromDto(responseDto);
	}

	private ApiResponse responseFromDto(ResponseDto responseDto) {
		List<QueryResult> results = new LinkedList<>();
		if (responseDto.results != null) {
			for (ResultDto resultDto : responseDto.results) {
				results.add(resultFromDto(resultDto));
			}
		}
		return new ApiResponse.Builder().results(results).build();
	}

	private QueryResult resultFromDto(ResultDto resultDto) {
		List<Series> series = new LinkedList<>();
		if (resultDto.series != null) {
			for (SeriesDto seriesDto : resultDto.series) {
				series.add(seriesFromDto(seriesDto));
			}
		}
		return new QueryResult.Builder().statementId(resultDto.statementId)
				.error(resultDto.error)
				.series(series)
				.build();
	}

	private Series seriesFromDto(SeriesDto seriesDto) {
		Set<String> tagSet = tagSetFromSeriesDto(seriesDto);
		List<Point> points = pointsFromSeriesDto(seriesDto);

		return new Series.Builder().name(seriesDto.name)
				.tags(tagSet)
				.points(points)
				.build();
	}

	private Set<String> tagSetFromSeriesDto(SeriesDto seriesDto) {
		Set<String> tagSet = new HashSet<>();
		if (seriesDto.columns != null && seriesDto.values != null && seriesDto.values.length > 0) {
			int startIndex = seriesDto.columns[0].equals("time") ? 1 : 0;
			for (int i = startIndex; i < seriesDto.columns.length; i++) {
				Object value = seriesDto.values[0][i];
				if (!(value instanceof Number || value instanceof Boolean)) {
					tagSet.add(seriesDto.columns[i]);
				}
			}
		}
		return tagSet;
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
