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
