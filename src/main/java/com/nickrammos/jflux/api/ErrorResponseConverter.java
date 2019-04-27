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

import com.nickrammos.jflux.api.response.ApiError;
import com.nickrammos.jflux.api.response.ApiResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Converts a response from a failed InfluxDB API call to an {@link ApiError}.
 */
final class ErrorResponseConverter implements Converter<Response<ApiResponse>, ApiError> {

	private static final Logger LOGGER =
			LoggerFactory.getLogger(ErrorResponseConverter.class);

	private final ObjectMapper objectMapper;

	ErrorResponseConverter() {
		objectMapper = new ObjectMapper();
	}

	@Override
	public ApiError convert(Response<ApiResponse> response) throws IOException {
		String errorMessage;
		if (response.errorBody() != null) {
			errorMessage = getErrorMessageFromErrorBody(response.errorBody());
		}
		else if (response.body() != null) {
			errorMessage = response.body().getErrorMessage();
		}
		else {
			errorMessage = null;
		}

		return new ApiError.Builder(response.code(), errorMessage).build();
	}

	private String getErrorMessageFromErrorBody(ResponseBody errorBody) throws IOException {
		String content = errorBody.string();
		if (content != null && content.endsWith("\n")) {
			content = content.substring(0, content.length() - 1);
		}

		LOGGER.debug("Converting {}", content);

		String errorMessage = objectMapper.readTree(content).get("error").toString();
		if (errorMessage.startsWith("\"")) {
			errorMessage = errorMessage.substring(1);
		}
		if (errorMessage.endsWith("\"")) {
			errorMessage = errorMessage.substring(0, errorMessage.length() - 1);
		}
		return errorMessage;
	}
}
