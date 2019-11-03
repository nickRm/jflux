package com.nickrammos.jflux;

import java.time.Instant;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Tag;
import com.nickrammos.jflux.annotation.Timestamp;
import com.nickrammos.jflux.domain.Point;
import com.nickrammos.jflux.exception.DuplicateAnnotatedMembersException;
import com.nickrammos.jflux.exception.InvalidAnnotatedType;
import com.nickrammos.jflux.exception.MissingAnnotatedMemberException;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationBasedPointConverterTest {

    private final AnnotationBasedPointConverter converter = new AnnotationBasedPointConverter(
            new NamingStrategy());

    @Test(expected = IllegalArgumentException.class)
    public void ctor_shouldThrowException_ifNamingStrategyIsNull() {
        new AnnotationBasedPointConverter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toPoint_shouldThrowException_ifInputIsNull() {
        converter.toPoint(null);
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

    @Test(expected = DuplicateAnnotatedMembersException.class)
    public void toPoint_shouldThrowException_ifMoreThanOneTimestampFieldsFound() {
        Object o = new Object() {
            @Timestamp
            private Instant t1 = Instant.now();

            @Timestamp
            private Instant t2 = Instant.now();

            @Field
            private int count;
        };
        converter.toPoint(o);
    }

    @Test(expected = InvalidAnnotatedType.class)
    public void toPoint_shouldThrowException_ifTimestampFieldTypeIsNotInstant() {
        Object o = new Object() {
            @Timestamp
            private int t;

            @Field
            private int count;
        };
        converter.toPoint(o);
    }

    @Test(expected = MissingAnnotatedMemberException.class)
    public void toPoint_shouldThrowException_ifNoFieldsFound() {
        Object o = new Object();
        converter.toPoint(o);
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

    @Test(expected = InvalidAnnotatedType.class)
    public void toPoint_shouldThrowException_ifFieldIsNotCorrectType() {
        Object o = new Object() {
            @Field
            private String text = "some text";
        };
        converter.toPoint(o);
    }
}
