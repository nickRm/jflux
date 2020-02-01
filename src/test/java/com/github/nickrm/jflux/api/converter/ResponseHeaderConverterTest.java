package com.github.nickrm.jflux.api.converter;

import com.github.nickrm.jflux.api.response.ResponseMetadata;
import com.github.nickrm.jflux.domain.BuildType;
import com.github.nickrm.jflux.domain.Version;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseHeaderConverterTest {

    private final ResponseHeaderConverter converter = new ResponseHeaderConverter();

    @Test
    public void test() {
        // Given
        String date = "Tue, 05 Nov 2018 16:08:32 GMT";
        String requestId = "9c353b0e-aadc-11e8-8023-000000000000";
        String buildType = "OSS";
        String version = "v1.7.0";

        Headers headers = new Headers.Builder()
                .add("Date", date)
                .add("X-Request-Id", requestId)
                .add("X-Influxdb-Build", buildType)
                .add("X-Influxdb-Version", version)
                .build();

        // When
        ResponseMetadata metadata = converter.convert(headers);

        // Then
        assertThat(metadata.getTimestamp()).isNotNull();
        assertThat(metadata.getRequestId()).isEqualTo(requestId);
        assertThat(metadata.getDbBuildType()).isEqualTo(BuildType.OPEN_SOURCE);
        assertThat(metadata.getDbVersion()).isEqualTo(Version.fromString(version));
    }
}
