package com.nickrammos.jflux.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.domain.Series;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Converts a {@link ResponseBody} from a call to the InfluxDB API, to an {@link ApiResponse}.
 */
final class InfluxResponseBodyConverter implements Converter<ResponseBody, ApiResponse> {

	private final ObjectMapper objectMapper;

	InfluxResponseBodyConverter() {
		objectMapper = new ObjectMapper();
	}

	@Override
	public ApiResponse convert(ResponseBody responseBody) throws IOException {
		String content = responseBody.string();
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
		return new QueryResult.Builder().statementId(resultDto.statementId).series(series).build();
	}

	private Series seriesFromDto(SeriesDto seriesDto) {
		List<List<Object>> values = new LinkedList<>();
		if (seriesDto.values != null) {
			for (Object[] val : seriesDto.values) {
				values.add(Arrays.asList(val));
			}
		}
		return new Series.Builder().name(seriesDto.name)
				.columns(Arrays.asList(seriesDto.columns))
				.values(values)
				.build();
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
