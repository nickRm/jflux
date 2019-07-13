package com.nickrammos.jflux.api.converter;

import java.io.IOException;

import com.nickrammos.jflux.api.response.ApiResponse;

import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ApiResponseConverterTest {

    @Mock
    private ResponseHeaderConverter responseHeaderConverter;

    @Mock
    private ResponseBodyConverter responseBodyConverter;

    @Mock
    private ErrorResponseConverter errorResponseConverter;

    private ApiResponseConverter apiResponseConverter;

    @Before
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
