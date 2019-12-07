package com.nickrammos.jflux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.nickrammos.jflux.domain.Point;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles conversions of {@link Point Points} to InfluxDB line protocol.
 *
 * @see <a href="https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_tutorial/">
 * InfluxDB line protocol</a>
 */
final class LineProtocolConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LineProtocolConverter.class);

    /**
     * Constructs line protocols from the specified points and measurement name.
     * <p>
     * Since the points can have different tag sets, the results of this method are grouped by tag
     * set. Points belonging to the same tag set are contained within a single line, while points
     * belonging to different tag sets are on different lines.
     *
     * @param measurementName the measurement to write to
     * @param points          the points to write
     *
     * @return the line protocols
     *
     * @throws IllegalArgumentException if the measurement name is blank
     */
    List<String> toLineProtocol(String measurementName, Collection<Point> points) {
        if (StringUtils.isBlank(measurementName)) {
            throw new IllegalArgumentException("Measurement name cannot be blank");
        }

        Map<String, Collection<Point>> series = new HashMap<>();
        points.forEach(point -> {
            Map<String, String> escapedTags = new HashMap<>();
            point.getTags().forEach((tagName, tagValue) -> {
                String escapedValue = tagValue.replaceAll("\\s", "\\\\ ");
                escapedTags.put(tagName, escapedValue);
            });
            String tagSet = collectKeyValuePairs(escapedTags);
            series.computeIfAbsent(tagSet, key -> new ArrayList<>()).add(point);
        });

        LOGGER.debug("Found {} tag sets: {}", series.size(), series.keySet());

        List<String> lineProtocols = new ArrayList<>();
        series.forEach((tagSet, seriesPoints) -> {
            String lineProtocol = toLineProtocol(measurementName, tagSet, seriesPoints);
            lineProtocols.add(lineProtocol);
        });

        return lineProtocols;
    }

    /**
     * Converts points with a specific tag set into line protocol.
     * <p>
     * Points will be collected into a single line for batch entry, separated by a new line as
     * specified by the InfluxDB line protocol format.
     *
     * @param measurementName the measurement that the points are to be written to
     * @param tagSet          the common tag set for the points
     * @param points          the points to convert
     *
     * @return the line protocol for the points
     */
    private String toLineProtocol(String measurementName, String tagSet, Collection<Point> points) {
        StringBuilder lineProtocol = new StringBuilder();
        lineProtocol.append(measurementName);

        if (!tagSet.isEmpty()) {
            lineProtocol.append(',').append(tagSet);
        }

        StringJoiner pointStringJoiner = new StringJoiner("\n");
        points.forEach(point -> {
            String fieldSet = collectKeyValuePairs(point.getFields());
            if (point.getTimestamp() != null) {
                fieldSet += " " + point.getTimestamp().toEpochMilli();
            }
            pointStringJoiner.add(fieldSet);
        });

        lineProtocol.append(' ').append(pointStringJoiner.toString());
        return lineProtocol.toString();
    }

    /**
     * Collects key value pairs from a map into a string in the format
     * {@code "key1"=value1,"key2"=value2}.
     *
     * @param map the map to collect
     *
     * @return the constructed string
     */
    private String collectKeyValuePairs(Map<String, ?> map) {
        return map.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }
}
