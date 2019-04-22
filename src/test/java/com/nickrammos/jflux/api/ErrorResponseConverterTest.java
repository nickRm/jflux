package com.nickrammos.jflux.api;

import java.io.IOException;
import java.util.Collections;

import com.nickrammos.jflux.api.response.ApiError;
import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.api.response.QueryResult;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseConverterTest {

	private ErrorResponseConverter converter = new ErrorResponseConverter();

	@Test
	public void convert_shouldGetErrorFromErrorBody_whenCallFailed() throws IOException {
		// Given
		int statusCode = 400;
		String errorMessage = "an error occurred";
		ResponseBody responseBody = ResponseBody.create(MediaType.get("application/json"),
				"{\"error\": \"" + errorMessage + "\"}");
		Response<ApiResponse> response = Response.error(statusCode, responseBody);

		// When
		ApiError apiError = converter.convert(response);

		// Then
		assertThat(apiError.getStatusCode()).isEqualTo(statusCode);
		assertThat(apiError.getMessage()).isEqualTo(errorMessage);
	}

	@Test
	public void convert_shouldGetErrorFromResponseBody_whenCallWasSuccessful() throws IOException {
		// Given
		String errorMessage = "an error occurred";
		QueryResult result = new QueryResult.Builder().error(errorMessage).build();
		ApiResponse apiResponse =
				new ApiResponse.Builder().results(Collections.singletonList(result)).build();
		Response<ApiResponse> response = Response.success(apiResponse);

		// When
		ApiError apiError = converter.convert(response);

		// Then
		assertThat(apiError.getMessage()).isEqualTo(errorMessage);
	}

	@Test
	public void convert_shouldReturnNullError_whenNoErrorExists() throws IOException {
		// Given
		Response<ApiResponse> response = Response.success(null);

		// When
		ApiError apiError = converter.convert(response);

		// Then
		assertThat(apiError.getMessage()).isNull();
	}
}
