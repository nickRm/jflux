package com.github.nickrm.jflux.api.converter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.github.nickrm.jflux.api.response.ApiResponse;
import com.github.nickrm.jflux.api.response.QueryResult;
import com.github.nickrm.jflux.api.response.ResponseMetadata;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Converts a {@link Response} to an {@link ApiResponse}.
 */
public final class ApiResponseConverter {

    private final ResponseHeaderConverter responseHeaderConverter;
    private final ResponseBodyConverter responseBodyConverter;
    private final ErrorResponseConverter errorResponseConverter;

    /**
     * Initializes a new instance.
     */
    public ApiResponseConverter() {
        this(new ResponseHeaderConverter(), new ResponseBodyConverter(),
                new ErrorResponseConverter());
    }

    /**
     * Initializes a new instance setting the converters.
     * <p>
     * This is currently mainly used for injecting mocks in testing.
     *
     * @param responseHeaderConverter used to extract metadata from the response
     * @param responseBodyConverter   used to extract the results from the response
     * @param errorResponseConverter  used to extract any errors from the response
     */
    ApiResponseConverter(ResponseHeaderConverter responseHeaderConverter,
            ResponseBodyConverter responseBodyConverter,
            ErrorResponseConverter errorResponseConverter) {
        this.responseHeaderConverter = responseHeaderConverter;
        this.responseBodyConverter = responseBodyConverter;
        this.errorResponseConverter = errorResponseConverter;
    }

    /**
     * Converts a response wrapper to an API response domain instance.
     *
     * @param responseWrapper the original wrapped response to convert
     *
     * @return the response converted to a domain instance
     *
     * @throws IOException if conversion fails
     */
    public ApiResponse convert(Response<ResponseBody> responseWrapper) throws IOException {
        ResponseMetadata metadata = responseHeaderConverter.convert(responseWrapper.headers());

        int statusCode = responseWrapper.code();
        String errorMessage = errorResponseConverter.convert(responseWrapper);
        List<QueryResult> results = responseWrapper.body() == null ?
                Collections.emptyList() :
                responseBodyConverter.convert(responseWrapper.body());

        return new ApiResponse.Builder().metadata(metadata)
                .statusCode(statusCode)
                .errorMessage(errorMessage)
                .results(results)
                .build();
    }
}
