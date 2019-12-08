package com.nickrammos.jflux;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Tag;
import com.nickrammos.jflux.annotation.Timestamp;
import com.nickrammos.jflux.annotation.exception.AnnotationProcessingException;
import com.nickrammos.jflux.annotation.exception.DuplicateAnnotatedMembersException;
import com.nickrammos.jflux.annotation.exception.InvalidAnnotatedType;
import com.nickrammos.jflux.annotation.exception.MissingAnnotatedMemberException;
import com.nickrammos.jflux.domain.Point;

/**
 * Handles conversions between annotated classes and {@link Point} instances.
 */
final class AnnotationBasedPointConverter {

    private final NamingStrategy namingStrategy;

    /**
     * Constructs a new converter with the specified naming strategy.
     *
     * @param namingStrategy the naming strategy to use, not {@code null}
     *
     * @throws IllegalArgumentException if {@code namingStrategy} is {@code null}
     */
    AnnotationBasedPointConverter(NamingStrategy namingStrategy) {
        if (namingStrategy == null) {
            throw new IllegalArgumentException("Naming strategy cannot be null");
        }

        this.namingStrategy = namingStrategy;
    }

    /**
     * Converts the specified object into a {@link Point} for writing to InfluxDB.
     *
     * @param annotatedObject the object to convert, not {@code null}
     *
     * @return the converted point
     *
     * @throws IllegalArgumentException      if {@code annotatedObject} is {@code null}
     * @throws AnnotationProcessingException if {@code annotatedObject} is not correctly annotated
     */
    Point toPoint(Object annotatedObject) {
        if (annotatedObject == null) {
            throw new IllegalArgumentException("Annotated object cannot be null");
        }

        Map<String, Object> fields = new HashMap<>();
        Map<String, String> tags = new HashMap<>();
        for (java.lang.reflect.Field field : annotatedObject.getClass().getDeclaredFields()) {
            Object fieldValue = getInstanceFieldValue(annotatedObject, field, Object.class);

            if (fieldValue == null || (!field.isAnnotationPresent(Field.class)
                    && !field.isAnnotationPresent(Tag.class))) {
                continue;
            }

            if (field.isAnnotationPresent(Field.class)) {
                if (!(fieldValue instanceof Number || fieldValue instanceof Boolean)) {
                    throw new InvalidAnnotatedType(field,
                            Arrays.asList(Number.class, Boolean.class));
                }
                fields.put(namingStrategy.getFieldName(field), fieldValue);
            }
            else {
                tags.put(namingStrategy.getTagName(field), fieldValue.toString());
            }
        }

        if (fields.isEmpty()) {
            throw new MissingAnnotatedMemberException(annotatedObject, Field.class);
        }

        Instant timestamp = getTimestamp(annotatedObject);

        return new Point.Builder().timestamp(timestamp).fields(fields).tags(tags).build();
    }

    private Instant getTimestamp(Object o) {
        java.lang.reflect.Field timestampField = Arrays.stream(o.getClass().getDeclaredFields())
                .filter(member -> member.isAnnotationPresent(Timestamp.class))
                .reduce((a, b) -> {
                    throw new DuplicateAnnotatedMembersException(o, Timestamp.class);
                })
                .orElse(null);

        if (timestampField == null) {
            return null;
        }

        return getInstanceFieldValue(o, timestampField, Instant.class);
    }

    private <T> T getInstanceFieldValue(Object o, java.lang.reflect.Field field, Class<T> type) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(o);
        } catch (IllegalAccessException e) {
            throw new AnnotationProcessingException("Could not get field value", e);
        } finally {
            field.setAccessible(false);
        }

        if (value == null) {
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        else {
            throw new InvalidAnnotatedType(field, type);
        }
    }

    /**
     * Converts a point to an instance of the specified type.
     * <p>
     * This method sets values for fields annotated with any of {@link Timestamp}, {@link Field},
     * or {@link Tag}. Class fields which are not annotated are not altered.
     *
     * @param point      the point to convert, not {@code null}
     * @param targetType the type to convert to, not {@code null}
     * @param <T>        the type to convert to
     *
     * @return an instance of the target type with the values from the point
     *
     * @throws IllegalArgumentException      if either {@code point} or {@code targetType} is
     *                                       {@code null}
     * @throws AnnotationProcessingException if the target class is not annotated correctly, cannot
     *                                       be instantiated, or its fields cannot be set
     */
    <T> T fromPoint(Point point, Class<T> targetType) {
        if (point == null) {
            throw new IllegalArgumentException("Point cannot be null");
        }

        if (targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null");
        }

        T targetInstance = createInstance(targetType);

        for (java.lang.reflect.Field field : targetType.getDeclaredFields()) {
            setFieldValue(point, targetInstance, field);
        }

        return targetInstance;
    }

    private <T> T createInstance(Class<T> targetType) {
        try {
            Constructor<T> ctor = targetType.getDeclaredConstructor();
            ctor.setAccessible(true);
            try {
                return ctor.newInstance();
            } finally {
                ctor.setAccessible(false);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new AnnotationProcessingException("Could not instantiate target class", e);
        }
    }

    private <T> void setFieldValue(Point point, T targetInstance, java.lang.reflect.Field field) {
        try {
            Object value;
            if (field.isAnnotationPresent(Timestamp.class)) {
                value = point.getTimestamp();
            }
            else if (field.isAnnotationPresent(Field.class)) {
                String fieldName = namingStrategy.getFieldName(field);
                value = point.getFields().get(fieldName);
            }
            else if (field.isAnnotationPresent(Tag.class)) {
                String tagName = namingStrategy.getTagName(field);
                value = point.getTags().get(tagName);
            }
            else {
                // Do not touch non-annotated fields.
                return;
            }

            field.setAccessible(true);
            try {
                field.set(targetInstance, value);
            } catch (IllegalArgumentException e) {
                throw new InvalidAnnotatedType(field, value.getClass());
            }
        } catch (IllegalAccessException e) {
            throw new AnnotationProcessingException("Could not set field value", e);
        } finally {
            field.setAccessible(false);
        }
    }
}
