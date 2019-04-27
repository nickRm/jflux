/*
 * Copyright 2019 Nick Rammos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nickrammos.jflux.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.nickrammos.jflux.api.response.ApiResponse;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Provides a converter from a {@link ResponseBody} to an {@link ApiResponse}.
 */
final class InfluxConverterFactory extends Converter.Factory {

	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
			Retrofit retrofit) {
		return new ResponseBodyConverter();
	}
}
