package com.nickrammos.jflux.api;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.nickrammos.jflux.api.converter.ApiResponseConverter;
import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.api.response.ResponseMetadata;
import com.nickrammos.jflux.domain.Measurement;
import com.nickrammos.jflux.domain.Point;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import retrofit2.Call;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JFluxHttpClientTest {

    @Mock
    private InfluxHttpService httpService;

    @Mock
    private ApiResponseConverter responseConverter;

    @InjectMocks
    private JFluxHttpClient client;

    @Test
    public void ping_shouldReturnMetadata_onSuccessfulResponse() throws IOException {
        // Given
        @SuppressWarnings("unchecked")
        Call<ResponseBody> call = Mockito.mock(Call.class);
        Response<ResponseBody> responseWrapper = Response.success(null);
        when(httpService.ping()).thenReturn(call);
        when(call.execute()).thenReturn(responseWrapper);

        ResponseMetadata metadata = new ResponseMetadata.Builder().build();
        ApiResponse apiResponse = new ApiResponse.Builder().metadata(metadata).build();
        when(responseConverter.convert(responseWrapper)).thenReturn(apiResponse);

        // When
        ResponseMetadata result = client.ping();

        // Then
        assertThat(result).isEqualTo(metadata);
    }

    @Test
    public void query_shouldReturnSingleSeries() throws IOException {
        // Given
        String query = "SELECT * FROM measurement_1";

        @SuppressWarnings("unchecked")
        Call<ResponseBody> call = Mockito.mock(Call.class);
        when(httpService.query(query)).thenReturn(call);
        ResponseBody responseBody = ResponseBody.create(MediaType.get("application/json"), "");
        Response<ResponseBody> responseWrapper = Response.success(responseBody);
        when(call.execute()).thenReturn(responseWrapper);

        ApiResponse response = createResponse();
        when(responseConverter.convert(responseWrapper)).thenReturn(response);

        // When
        Measurement measurement = client.query(query);

        // Then
        assertThat(measurement).isEqualTo(response.getResults().get(0).getResults().get(0));
    }

    @Test
    public void query_shouldReturnNull_ifNoResults() throws IOException {
        // Given
        String query = "SELECT * FROM non_existent_measurement";

        @SuppressWarnings("unchecked")
        Call<ResponseBody> call = Mockito.mock(Call.class);
        when(httpService.query(query)).thenReturn(call);
        ResponseBody responseBody = ResponseBody.create(MediaType.get("application/json"), "");
        Response<ResponseBody> responseWrapper = Response.success(responseBody);
        when(call.execute()).thenReturn(responseWrapper);

        QueryResult queryResult = new QueryResult.Builder().build();
        ApiResponse response =
                new ApiResponse.Builder().results(Collections.singletonList(queryResult)).build();
        when(responseConverter.convert(responseWrapper)).thenReturn(response);

        // When
        Measurement measurement = client.query(query);

        // Then
        assertThat(measurement).isNull();
    }

    @Test
    public void query_shouldThrowException_ifQueriesMultipleMeasurements() {
        String query = "SELECT * FROM measurement_1, measurement_2";
        assertThatIllegalArgumentException().isThrownBy(() -> client.query(query));
    }

    @Test
    public void query_shouldThrowException_ifQueryIsMultiStatement() {
        String query = "SELECT * FROM measurement_1; SELECT * FROM measurement_2";
        assertThatIllegalArgumentException().isThrownBy(() -> client.query(query));
    }

    @Test
    public void query_shouldThrowException_ifQueryIsSelectInto() {
        String query = "SELECT * INTO measurement_1 FROM measurement_2";
        assertThatIllegalArgumentException().isThrownBy(() -> client.query(query));
    }

    @Test
    public void multiSeriesQuery_shouldReturnSingleResult() throws IOException {
        // Given
        String query = "SELECT * FROM measurement_1";

        @SuppressWarnings("unchecked")
        Call<ResponseBody> call = Mockito.mock(Call.class);
        when(httpService.query(query)).thenReturn(call);
        ResponseBody responseBody = ResponseBody.create(MediaType.get("application/json"), "");
        Response<ResponseBody> responseWrapper = Response.success(responseBody);
        when(call.execute()).thenReturn(responseWrapper);

        ApiResponse response = createResponse();
        when(responseConverter.convert(responseWrapper)).thenReturn(response);

        // When
        QueryResult result = client.queryMultipleSeries(query);

        // Then
        assertThat(result).isEqualTo(response.getResults().get(0));
    }

    @Test
    public void multiSeriesQuery_shouldThrowException_ifQueryIsMultiStatement() {
        String query = "SELECT * FROM measurement_1; SELECT * FROM measurement_2";
        assertThatIllegalArgumentException().isThrownBy(() -> client.queryMultipleSeries(query));
    }

    @Test
    public void multiSeriesQuery_shouldThrowException_ifQueryIsSelectInto() {
        String query = "SELECT * INTO measurement_1 FROM measurement_2";
        assertThatIllegalArgumentException().isThrownBy(() -> client.queryMultipleSeries(query));
    }

    @Test
    public void multiResultQuery_shouldReturnResponse() throws IOException {
        // Given
        String query = "SELECT * FROM measurement_1";

        @SuppressWarnings("unchecked")
        Call<ResponseBody> call = Mockito.mock(Call.class);
        when(httpService.query(query)).thenReturn(call);
        ResponseBody responseBody = ResponseBody.create(MediaType.get("application/json"), "");
        Response<ResponseBody> responseWrapper = Response.success(responseBody);
        when(call.execute()).thenReturn(responseWrapper);

        ApiResponse response = createResponse();
        when(responseConverter.convert(responseWrapper)).thenReturn(response);

        // When
        ApiResponse actualResponse = client.batchQuery(query);

        // Then
        assertThat(actualResponse).isEqualTo(response);
    }

    @Test
    public void multiResultQuery_shouldThrowException_ifQueryIsSelectInto() {
        String query = "SELECT * INTO measurement_1 FROM measurement_2";
        assertThatIllegalArgumentException().isThrownBy(() -> client.batchQuery(query));
    }

    private static ApiResponse createResponse() {
        List<Point> points = Collections.singletonList(new Point.Builder().build());
        Measurement measurement = new Measurement.Builder().name("series").points(points).build();

        QueryResult result = new QueryResult.Builder().statementId(0)
                .series(Collections.singletonList(measurement))
                .build();

        return new ApiResponse.Builder().results(Collections.singletonList(result)).build();
    }
}
