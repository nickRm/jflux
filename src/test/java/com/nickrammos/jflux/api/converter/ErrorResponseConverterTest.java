package com.nickrammos.jflux.api.converter;

import java.io.IOException;

import com.nickrammos.jflux.api.response.ApiResponse;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseConverterTest {

    private ErrorResponseConverter converter = new ErrorResponseConverter();

    @Test
    public void convert_shouldGetErrorFromErrorBody_whenCallFailed() throws IOException {
        // Given
        String errorMessage = "an error occurred";
        ResponseBody responseBody = ResponseBody.create(MediaType.get("application/json"),
                "{\"error\": \"" + errorMessage + "\"}");
        Response<ApiResponse> response = Response.error(400, responseBody);

        // When
        String result = converter.convert(response);

        // Then
        assertThat(result).isEqualTo(errorMessage);
    }

    @Test
    public void convert_shouldReturnNullError_whenNoErrorExists() throws IOException {
        // Given
        Response<ApiResponse> response = Response.success(null);

        // When
        String result = converter.convert(response);

        // Then
        assertThat(result).isNull();
    }
}
