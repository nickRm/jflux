package com.github.nickrm.jflux.api.converter;

import java.io.IOException;

import com.github.nickrm.jflux.api.response.ApiResponse;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ApiResponseConverterTest {

    @Mock
    private ResponseHeaderConverter responseHeaderConverter;

    @Mock
    private ResponseBodyConverter responseBodyConverter;

    @Mock
    private ErrorResponseConverter errorResponseConverter;

    private ApiResponseConverter apiResponseConverter;

    @BeforeEach
    public void setup() {
        apiResponseConverter =
                new ApiResponseConverter(responseHeaderConverter, responseBodyConverter,
                        errorResponseConverter);
    }

    @Test
    public void convert_shouldSetEmptyResults_ifBodyIsNull() throws IOException {
        // Given
        Response<ResponseBody> responseWrapper = Response.success(null);

        // When
        ApiResponse apiResponse = apiResponseConverter.convert(responseWrapper);

        // Then
        assertThat(apiResponse.getResults()).isNotNull();
        assertThat(apiResponse.getResults()).isEmpty();
    }
}
