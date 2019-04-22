package com.nickrammos.jflux.api;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.nickrammos.jflux.api.response.ApiResponse;
import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.Series;
import com.nickrammos.jflux.exception.InvalidQueryException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Call;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JFluxHttpClientTest {

	@Mock
	private InfluxHttpService httpService;

	@InjectMocks
	private JFluxHttpClient client;

	@Test
	public void isConnected_shouldReturnTrue_onSuccessfulResponse() throws IOException {
		// Given
		@SuppressWarnings("unchecked")
		Call<ResponseBody> call = Mockito.mock(Call.class);
		when(httpService.ping()).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(null));

		// When
		boolean connected = client.isConnected();

		// Then
		assertThat(connected).isTrue();
	}

	@Test
	public void isConnected_shouldReturnFalse_onUnsuccessfulResponse() throws IOException {
		// Given
		@SuppressWarnings("unchecked")
		Call<ResponseBody> call = Mockito.mock(Call.class);
		when(httpService.ping()).thenReturn(call);
		when(call.execute()).thenReturn(
				Response.error(500, ResponseBody.create(MediaType.get("application/json"), "")));

		// When
		boolean connected = client.isConnected();

		// Then
		assertThat(connected).isFalse();
	}

	@Test
	public void isConnected_shouldReturnFalse_onException() throws IOException {
		// Given
		@SuppressWarnings("unchecked")
		Call<ResponseBody> call = Mockito.mock(Call.class);
		when(httpService.ping()).thenReturn(call);
		when(call.execute()).thenThrow(new IOException());

		// When
		boolean connected = client.isConnected();

		// Then
		assertThat(connected).isFalse();
	}

	@Test
	public void query_shouldReturnSingleSeries() throws IOException {
		// Given
		String query = "SELECT * FROM measurement_1";
		ApiResponse response = createResponse();

		@SuppressWarnings("unchecked")
		Call<ApiResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(response));

		// When
		Series series = client.query(query);

		// Then
		assertThat(series).isEqualTo(response.getResults().get(0).getSeries().get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void query_shouldThrowException_ifQueriesMultipleMeasurements() throws IOException {
		String query = "SELECT * FROM measurement_1, measurement_2";
		client.query(query);
	}

	@Test(expected = IllegalArgumentException.class)
	public void query_shouldThrowException_ifQueryIsMultiStatement() throws IOException {
		String query = "SELECT * FROM measurement_1; SELECT * FROM measurement_2";
		client.query(query);
	}

	@Test(expected = IllegalArgumentException.class)
	public void query_shouldThrowException_ifQueryIsSelectInto() throws IOException {
		String query = "SELECT * INTO measurement_1 FROM measurement_2";
		client.query(query);
	}

	@Test(expected = InvalidQueryException.class)
	public void query_shouldThrowException_ifQuerySyntaxIsInvalid() throws IOException {
		// Given
		String query = "SELECT * FROM FROM measurement_1";
		ResponseBody errorBody = ResponseBody.create(MediaType.get("application/json"),
				"{\"error\": \"some error\"}");
		Response<ApiResponse> errorResponse = Response.error(400, errorBody);

		@SuppressWarnings("unchecked")
		Call<ApiResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(errorResponse);

		// When
		client.query(query);

		// Then
		// Expect exception
	}

	@Test(expected = InvalidQueryException.class)
	public void query_shouldThrowException_ifQueryIsInvalid() throws IOException {
		// Given
		String query = "SELECT * FROM non_existent_measurement";
		QueryResult queryResult = new QueryResult.Builder().error("some error").build();
		ApiResponse apiResponse =
				new ApiResponse.Builder().results(Collections.singletonList(queryResult)).build();

		@SuppressWarnings("unchecked")
		Call<ApiResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(apiResponse));

		// When
		client.query(query);

		// Then
		// Expect exception
	}

	@Test
	public void multiSeriesQuery_shouldReturnSingleResult() throws IOException {
		// Given
		String query = "SELECT * FROM measurement_1";
		ApiResponse response = createResponse();

		@SuppressWarnings("unchecked")
		Call<ApiResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(response));

		// When
		QueryResult result = client.queryMultipleSeries(query);

		// Then
		assertThat(result).isEqualTo(response.getResults().get(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void multiSeriesQuery_shouldThrowException_ifQueryIsMultiStatement() throws IOException {
		String query = "SELECT * FROM measurement_1; SELECT * FROM measurement_2";
		client.queryMultipleSeries(query);
	}

	@Test(expected = IllegalArgumentException.class)
	public void multiSeriesQuery_shouldThrowException_ifQueryIsSelectInto() throws IOException {
		String query = "SELECT * INTO measurement_1 FROM measurement_2";
		client.queryMultipleSeries(query);
	}

	@Test
	public void multiResultQuery_shouldReturnResponse() throws IOException {
		// Given
		String query = "SELECT * FROM measurement_1";
		ApiResponse response = createResponse();

		@SuppressWarnings("unchecked")
		Call<ApiResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(response));

		// When
		ApiResponse actualResponse = client.batchQuery(query);

		// Then
		assertThat(actualResponse).isEqualTo(response);
	}

	@Test(expected = IllegalArgumentException.class)
	public void multiResultQuery_shouldThrowException_ifQueryIsSelectInto() throws IOException {
		String query = "SELECT * INTO measurement_1 FROM measurement_2";
		client.batchQuery(query);
	}

	private static ApiResponse createResponse() {
		Set<String> tags = Collections.singleton("tag");
		List<Point> points = Collections.singletonList(new Point.Builder().build());
		Series series = new Series.Builder().name("series").tags(tags).points(points).build();

		QueryResult result = new QueryResult.Builder().statementId(0)
				.series(Collections.singletonList(series))
				.build();

		return new ApiResponse.Builder().results(Collections.singletonList(result)).build();
	}
}
