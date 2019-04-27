/*
 * Copyright 2019 Nick Rammos
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nickrammos.jflux.api;

import com.nickrammos.jflux.api.response.ApiResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

	/**
	 * Executes DDL statements against InfluxDB.
	 *
	 * @param statement the statement to execute
	 *
	 * @return the execution result
	 */
	@POST("/query")
	Call<ApiResponse> alter(@Query("q") String statement);
}
