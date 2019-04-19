package com.nickrammos.jflux.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

interface InfluxHttpService {

	@GET("/ping")
	Call<ResponseBody> ping();
}
