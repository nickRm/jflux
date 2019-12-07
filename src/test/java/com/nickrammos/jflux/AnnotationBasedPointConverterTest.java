package com.nickrammos.jflux;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Tag;
import com.nickrammos.jflux.annotation.Timestamp;
import com.nickrammos.jflux.annotation.exception.DuplicateAnnotatedMembersException;
import com.nickrammos.jflux.annotation.exception.InvalidAnnotatedType;
import com.nickrammos.jflux.annotation.exception.MissingAnnotatedMemberException;
import com.nickrammos.jflux.domain.Point;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class AnnotationBasedPointConverterTest {

    private final AnnotationBasedPointConverter converter = new AnnotationBasedPointConverter(
            new NamingStrategy());

    @Test
    public void ctor_shouldThrowException_ifNamingStrategyIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new AnnotationBasedPointConverter(null));
    }

    @Test
    public void toPoint_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> converter.toPoint(null));
    }

    @Test
    public void toPoint_shouldSetTheTimestamp() {
        Instant timestamp = Instant.now();
        Object o = new Object() {
            @Timestamp
            private final Instant t = timestamp;

            @Field
            private int count;
        };
        Point point = converter.toPoint(o);
        assertThat(point.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    public void toPoint_shouldSetNullTimestampIfFieldNotSet() {
        Object o = new Object() {
            @Timestamp
            private Instant t;

            @Field
            private int count;
        };
        Point point = converter.toPoint(o);
        assertThat(point.getTimestamp()).isNull();
    }

    @Test
    public void toPoint_shouldSetNullTimestamp_ifNoTimestampFieldsFound() {
        Object o = new Object() {
            @Field
            private int count;
        };
        Point point = converter.toPoint(o);
        assertThat(point.getTimestamp()).isNull();
    }

    @Test
    public void toPoint_shouldThrowException_ifMoreThanOneTimestampFieldsFound() {
        Object o = new Object() {
            @Timestamp
            private Instant t1 = Instant.now();

            @Timestamp
            private Instant t2 = Instant.now();

            @Field
            private int count;
        };
        assertThatExceptionOfType(DuplicateAnnotatedMembersException.class).isThrownBy(() ->
                converter.toPoint(o));
    }

    @Test
    public void toPoint_shouldThrowException_ifTimestampFieldTypeIsNotInstant() {
        Object o = new Object() {
            @Timestamp
            private int t;

            @Field
            private int count;
        };
        assertThatExceptionOfType(InvalidAnnotatedType.class).isThrownBy(() ->
                converter.toPoint(o));
    }

    @Test
    public void toPoint_shouldThrowException_ifNoFieldsFound() {
        Object o = new Object();
        assertThatExceptionOfType(MissingAnnotatedMemberException.class).isThrownBy(
                () -> converter.toPoint(o));
    }

    @Test
    public void toPoint_shouldIgnoreFieldsWithNullValue() {
        Object o = new Object() {
            @Field
            private int valueField = 0;

            @Field
            private Integer nullField = null;

            @Tag
            private String nullTag = null;
        };
        Point point = converter.toPoint(o);
        assertThat(point.getFields()).containsOnlyKeys("value_field");
        assertThat(point.getTags()).isEmpty();
    }

    @Test
    public void toPoint_shouldThrowException_ifFieldIsNotCorrectType() {
        Object o = new Object() {
            @Field
            private String text = "some text";
        };
        assertThatExceptionOfType(InvalidAnnotatedType.class).isThrownBy(
                () -> converter.toPoint(o));
    }

    @Test
    public void fromPoint_shouldThrowException_ifPointIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> converter.fromPoint(null, Object.class));
    }

    @Test
    public void fromPoint_shouldThrowException_ifTargetTypeIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> converter.fromPoint(new Point.Builder().build(), null));
    }

    @Test
    public void fromPoint_shouldSetTimestampCorrectly() {
        // Given
        Instant timestamp = Instant.now();
        Point point = new Point.Builder().timestamp(timestamp).build();

        // When
        TestAnnotatedClass result = converter.fromPoint(point, TestAnnotatedClass.class);

        // Then
        assertThat(result.timestamp).isEqualTo(timestamp);
    }

    @Test
    public void fromPoint_shouldThrowException_ifTimestampIsIncorrectType() {
        Point point = new Point.Builder().timestamp(Instant.now()).build();
        assertThatExceptionOfType(InvalidAnnotatedType.class).isThrownBy(
                () -> converter.fromPoint(point, TestAnnotatedClassWithDateAsTimestamp.class));
    }

    @Test
    public void fromPoint_shouldSetFieldsCorrectly() {
        // Given
        int fieldValue = 123;
        Map<String, Object> fields = Collections.singletonMap("a_field", fieldValue);
        Point point = new Point.Builder().fields(fields).build();

        // When
        TestAnnotatedClass result = converter.fromPoint(point, TestAnnotatedClass.class);

        // Then
        assertThat(result.aField).isEqualTo(fieldValue);
    }

    @Test
    public void fromPoint_shouldThrowException_ifFieldIsIncorrectType() {
        // Given
        Map<String, Object> fields = Collections.singletonMap("a_field", true);
        Point point = new Point.Builder().fields(fields).build();

        // When/Then
        assertThatExceptionOfType(InvalidAnnotatedType.class).isThrownBy(
                () -> converter.fromPoint(point, TestAnnotatedClass.class));
    }

    @Test
    public void fromPoint_shouldSetTagsCorrectly() {
        // Given
        String tagValue = "tag_value";
        Map<String, String> tags = Collections.singletonMap("a_tag", tagValue);
        Point point = new Point.Builder().tags(tags).build();

        // When
        TestAnnotatedClass result = converter.fromPoint(point, TestAnnotatedClass.class);

        // Then
        assertThat(result.aTag).isEqualTo(tagValue);
    }

    @Test
    public void fromPoint_shouldThrowException_ifTagIsIncorrectType() {
        // Given
        Map<String, String> tags = Collections.singletonMap("a_tag", "tag_value");
        Point point = new Point.Builder().tags(tags).build();

        // When/Then
        assertThatExceptionOfType(InvalidAnnotatedType.class).isThrownBy(
                () -> converter.fromPoint(point, TestAnnotatedClassWithNonStringTag.class));
    }

    @Test
    public void fromPoint_shouldNotAffectNonAnnotatedFields() {
        // Given
        Point point = new Point.Builder().build();

        // When
        TestAnnotatedClass result = converter.fromPoint(point, TestAnnotatedClass.class);

        // Then
        assertThat(result.nonAnnotatedField).isEqualTo("default_value");
    }

    private static class TestAnnotatedClass {

        @Timestamp
        private Instant timestamp;

        @Field
        private Integer aField;

        @Tag
        private String aTag;

        private String nonAnnotatedField = "default_value";
    }

    private static class TestAnnotatedClassWithDateAsTimestamp {

        @Timestamp
        private Date timestamp;
    }

    private static class TestAnnotatedClassWithNonStringTag {

        @Tag
        private Integer aTag;
    }
}
