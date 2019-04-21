package com.nickrammos.jflux.api;

import com.nickrammos.jflux.api.response.ApiResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Provides the definition for the InfluxDB HTTP API.
 */
interface InfluxHttpService {

	@GET("/ping")
	Call<ResponseBody> ping();

	/**
	 * Queries data from InfluxDB.
	 *
	 * @param query the query to execute
	 *
	 * @return the query result
	 */
	@GET("/query")
	Call<ApiResponse> query(@Query("q") String query);
}
