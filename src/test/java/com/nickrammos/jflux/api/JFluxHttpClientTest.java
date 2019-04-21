package com.nickrammos.jflux.api;

import java.io.IOException;

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
}
