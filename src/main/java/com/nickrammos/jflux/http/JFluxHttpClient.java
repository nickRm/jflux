package com.nickrammos.jflux.http;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Makes calls to the InfluxDB HTTP API.
 *
 * @see Builder
 */
public final class JFluxHttpClient implements AutoCloseable {

	private final InfluxHttpService service;

	/**
	 * Initializes a new instance setting the service to be used for calls to the API.
	 *
	 * @param service the API service
	 */
	JFluxHttpClient(InfluxHttpService service) {
		this.service = service;
	}

	/**
	 * Tests the connection to the InfluxDB API and returns the result.
	 *
	 * @return {@code true} if the API is reachable, {@code false} otherwise
	 */
	public boolean isConnected() {
		Call<ResponseBody> call = service.ping();
		try {
			Response<ResponseBody> response = call.execute();
			return response.isSuccessful();
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public void close() {
		// Nothing to close for now.
	}

	/**
	 * Used to construct {@link JFluxHttpClient} instances.
	 */
	public static final class Builder {

		private String host;

		/**
		 * Initializes a new builder instance, setting the InfluxDB host URL.
		 *
		 * @param host the InfluxDB host URL, e.g. {@code http://localhost:8086}
		 */
		public Builder(String host) {
			this.host = host;
		}

		/**
		 * Constructs a new {@link JFluxHttpClient} instance from this builder's configuration.
		 *
		 * @return the new client instance
		 */
		public JFluxHttpClient build() {
			Retrofit retrofit = new Retrofit.Builder().baseUrl(host).build();
			InfluxHttpService service = retrofit.create(InfluxHttpService.class);
			return new JFluxHttpClient(service);
		}
	}
}
