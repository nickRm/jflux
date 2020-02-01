package com.github.nickrm.jflux.api.converter;

import java.time.Instant;
import java.util.Date;

import com.github.nickrm.jflux.api.response.ResponseMetadata;
import com.github.nickrm.jflux.domain.BuildType;
import com.github.nickrm.jflux.domain.Version;
import okhttp3.Headers;
import retrofit2.Converter;

/**
 * Converts response headers into {@link ResponseMetadata}.
 */
final class ResponseHeaderConverter implements Converter<Headers, ResponseMetadata> {

    private static final String DATE_HEADER_NAME = "Date";
    private static final String REQUEST_ID_HEADER_NAME = "X-Request-Id";
    private static final String BUILD_TYPE_HEADER_NAME = "X-Influxdb-Build";
    private static final String VERSION_HEADER_NAME = "X-Influxdb-Version";

    @Override
    public ResponseMetadata convert(Headers headers) {
        Date date = headers.getDate(DATE_HEADER_NAME);
        Instant timestamp = date == null ? Instant.now() : date.toInstant();
        String requestId = headers.get(REQUEST_ID_HEADER_NAME);
        BuildType buildType = BuildType.fromIdentifier(headers.get(BUILD_TYPE_HEADER_NAME));
        Version version = Version.fromString(headers.get(VERSION_HEADER_NAME));

        return new ResponseMetadata.Builder().timestamp(timestamp)
                .requestId(requestId)
                .dbBuildType(buildType)
                .dbVersion(version)
                .build();
    }
}
