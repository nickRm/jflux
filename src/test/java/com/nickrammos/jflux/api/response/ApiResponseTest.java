package com.nickrammos.jflux.api.response;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiResponseTest {

    @Test
    public void hasError_shouldReturnTrue_ifOneResultHasError() {
        String errorMessage = "error";
        QueryResult result = new QueryResult.Builder().error(errorMessage).build();
        ApiResponse apiResponse =
                new ApiResponse.Builder().results(Collections.singletonList(result)).build();

        assertThat(apiResponse.hasError()).isTrue();
    }

    @Test
    public void hasError_shouldReturnFalse_ifNoResultsHaveError() {
        QueryResult result = new QueryResult.Builder().build();
        ApiResponse apiResponse =
                new ApiResponse.Builder().results(Collections.singletonList(result)).build();

        assertThat(apiResponse.hasError()).isFalse();
    }

    @Test
    public void hasError_shouldReturnFalse_ifNoResultsPresent() {
        ApiResponse apiResponse = new ApiResponse.Builder().build();
        assertThat(apiResponse.hasError()).isFalse();
    }

    @Test
    public void getErrorMessage_shouldReturnErrorMessage_ifOneResultHasError() {
        String errorMessage = "error";
        QueryResult result = new QueryResult.Builder().error(errorMessage).build();
        ApiResponse apiResponse =
                new ApiResponse.Builder().results(Collections.singletonList(result)).build();

        assertThat(apiResponse.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    public void getErrorMessage_shouldReturnFirstMessage_ifMultipleResultsHaveError() {
        String firstErrorMessage = "error_1";
        QueryResult firstResult = new QueryResult.Builder().error(firstErrorMessage).build();
        String secondErrorMessage = "error_2";
        QueryResult secondResult = new QueryResult.Builder().error(secondErrorMessage).build();

        ApiResponse apiResponse =
                new ApiResponse.Builder().results(Arrays.asList(firstResult, secondResult)).build();

        assertThat(apiResponse.getErrorMessage()).isEqualTo(firstErrorMessage);
    }

    @Test
    public void getErrorMessage_shouldReturnNull_ifNoResultsHaveErrors() {
        QueryResult result = new QueryResult.Builder().build();
        ApiResponse apiResponse =
                new ApiResponse.Builder().results(Collections.singletonList(result)).build();

        assertThat(apiResponse.getErrorMessage()).isNull();
    }

    @Test
    public void getErrorMessage_shouldReturnNull_ifNoResultsPresent() {
        ApiResponse apiResponse = new ApiResponse.Builder().build();
        assertThat(apiResponse.getErrorMessage()).isNull();
    }
}
