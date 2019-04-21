package com.nickrammos.jflux.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Provides the definition for the InfluxDB HTTP API.
 */
interface InfluxHttpService {

	@GET("/ping")
	Call<ResponseBody> ping();
}
