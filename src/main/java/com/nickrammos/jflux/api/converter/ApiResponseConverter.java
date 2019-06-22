package com.nickrammos.jflux.api.converter;

import java.io.IOException;

import com.nickrammos.jflux.api.response.ApiError;
import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.exception.InvalidQueryException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Converts a {@link Response} to an {@link ApiResponse}.
 */
public class ApiResponseConverter {

    private final ResponseBodyConverter responseBodyConverter;
    private final ErrorResponseConverter errorResponseConverter;

    public ApiResponseConverter() {
        responseBodyConverter = new ResponseBodyConverter();
        errorResponseConverter = new ErrorResponseConverter();
    }

    public ApiResponse convert(Response<ResponseBody> responseWrapper) throws IOException {
        ApiResponse response = responseWrapper.body() == null ?
                new ApiResponse.Builder().build() :
                responseBodyConverter.convert(responseWrapper.body());

        if (!responseWrapper.isSuccessful()) {
            ApiError apiError = errorResponseConverter.convert(responseWrapper);
            throw new InvalidQueryException(apiError.getMessage());
        }

        return response;
    }
}
