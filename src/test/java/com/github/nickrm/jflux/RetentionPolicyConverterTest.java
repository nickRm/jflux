package com.github.nickrm.jflux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.github.nickrm.jflux.domain.Point;
import com.github.nickrm.jflux.domain.RetentionPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class RetentionPolicyConverterTest {

    @Test
    public void parsePoint_shouldParseFields() {
        // Given
        String name = "test_rp";
        int durationHours = 24;
        int replication = 2;
        boolean isDefault = true;

        Map<String, String> tags = new HashMap<>();
        tags.put("name", name);
        tags.put("duration", durationHours + "h0m0s");
        tags.put("shardGroupDuration", "0s");
        Map<String, Object> fields = new HashMap<>();
        fields.put("replicaN", replication);
        fields.put("default", isDefault);
        Point point = new Point.Builder().tags(tags).fields(fields).build();

        // When
        RetentionPolicy actualRetentionPolicy = new RetentionPolicyConverter().parsePoint(point);

        // Then
        RetentionPolicy expectedRetentionPolicy =
                new RetentionPolicy.Builder(name, Duration.ofHours(durationHours)).replication(
                        replication).shardDuration(Duration.ZERO).isDefault(isDefault).build();

        assertThat(actualRetentionPolicy).isEqualTo(expectedRetentionPolicy);
    }

    @Test
    public void parsePoint_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new RetentionPolicyConverter().parsePoint(null));
    }
}
