package com.nickrammos.jflux.api;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.nickrammos.jflux.api.response.InfluxResponse;
import com.nickrammos.jflux.api.response.InfluxResult;
import com.nickrammos.jflux.domain.Series;

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
		InfluxResponse response = createResponse();

		@SuppressWarnings("unchecked")
		Call<InfluxResponse> call = Mockito.mock(Call.class);
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

	@Test
	public void multiSeriesQuery_shouldReturnSingleResult() throws IOException {
		// Given
		String query = "SELECT * FROM measurement_1";
		InfluxResponse response = createResponse();

		@SuppressWarnings("unchecked")
		Call<InfluxResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(response));

		// When
		InfluxResult result = client.queryMultipleSeries(query);

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
		InfluxResponse response = createResponse();

		@SuppressWarnings("unchecked")
		Call<InfluxResponse> call = Mockito.mock(Call.class);
		when(httpService.query(query)).thenReturn(call);
		when(call.execute()).thenReturn(Response.success(response));

		// When
		InfluxResponse actualResponse = client.batchQuery(query);

		// Then
		assertThat(actualResponse).isEqualTo(response);
	}

	@Test(expected = IllegalArgumentException.class)
	public void multiResultQuery_shouldThrowException_ifQueryIsSelectInto() throws IOException {
		String query = "SELECT * INTO measurement_1 FROM measurement_2";
		client.batchQuery(query);
	}

	private static InfluxResponse createResponse() {
		List<String> columns = Collections.singletonList("column");
		List<List<Object>> values = Collections.singletonList(Collections.singletonList("value"));
		Series series =
				new Series.Builder().name("series").columns(columns).values(values).build();

		InfluxResult result = new InfluxResult.Builder().statementId(0)
				.series(Collections.singletonList(series))
				.build();

		return new InfluxResponse.Builder().results(Collections.singletonList(result)).build();
	}
}
