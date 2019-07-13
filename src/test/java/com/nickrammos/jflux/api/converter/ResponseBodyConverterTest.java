package com.nickrammos.jflux.api.converter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.nickrammos.jflux.api.response.QueryResult;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.domain.Series;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseBodyConverterTest {

    private ResponseBodyConverter converter = new ResponseBodyConverter();

    @Test
    public void test() throws IOException {
        // Given
        String measurementName = "measurement_1";
        Instant timestamp = Instant.now();
        String tagName = "tag_1";
        String fieldName = "field_1";
        String tagValue = "tag";
        Object fieldValue = 1;

        // @formatter:off
		String content =
				"{"
					+ "\"results\": ["
						+ "{"
							+ "\"statementId\": 0,"
							+ "\"series\": ["
								+ "{"
									+ "\"name\": \"" + measurementName + "\","
									+ "\"columns\": [\"time\", \"" + tagName + "\", \"" + fieldName + "\"],"
									+ "\"values\": ["
										+ "[\"" + timestamp + "\", \"" + tagValue + "\", " + fieldValue + "]"
									+ "]"
								+ "}"
							+ "]"
						+ "}"
					+ "]"
				+ "}";
		// @formatter:on

        // When
        List<QueryResult> response =
                converter.convert(ResponseBody.create(MediaType.get("application/json"), content));

        // Then
        assertThat(response).isNotNull();

        Series series = response.get(0).getSeries().get(0);
        assertThat(series.getName()).isEqualTo(measurementName);
        assertThat(series.getTags()).containsExactly(tagName);

        Point point = series.getPoints().get(0);
        assertThat(point.getTimestamp()).isEqualTo(timestamp);
        assertThat(point.getTags()).containsOnlyKeys(tagName);
        assertThat(point.getTags().get(tagName)).isEqualTo(tagValue);
        assertThat(point.getFields()).containsOnlyKeys(fieldName);
        assertThat(point.getFields().get(fieldName)).isEqualTo(fieldValue);
    }
}
