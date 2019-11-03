package com.nickrammos.jflux;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Measurement;
import com.nickrammos.jflux.annotation.Tag;

/**
 * Defines how the names of measurements, fields, and tags, are derived from Java objects for
 * writing into InfluxDB.
 */
final class NamingStrategy {

    /**
     * Gets the measurement name for the specified class.
     * <p>
     * The measurement name will be the value of the {@link Measurement} annotation if present on
     * the class, otherwise it will be the class name converted to snake_case.
     *
     * @param measurementClass the class to get the name for, not {@code null}
     *
     * @return the measurement name
     *
     * @throws IllegalArgumentException if {@code measurementClass} is {@code null}
     */
    String getMeasurementName(Class<?> measurementClass) {
        if (measurementClass == null) {
            throw new IllegalArgumentException("Class cannot be null");
        }

        if (measurementClass.isAnnotationPresent(Measurement.class)
                && !measurementClass.getAnnotation(Measurement.class).value().isEmpty()) {
            return measurementClass.getAnnotation(Measurement.class).value();
        }
        else {
            return toSnakeCase(measurementClass.getSimpleName());
        }
    }

    /**
     * Gets the field name for the specified instance field.
     * <p>
     * The field name will be the value of the {@link Field} annotation if present on the field,
     * otherwise it will be the instance field name converted to snake_case.
     *
     * @param instanceField the field to get the name for, not {@code null}
     *
     * @return the field name
     *
     * @throws IllegalArgumentException if {@code instanceField} is {@code null}
     */
    String getFieldName(java.lang.reflect.Field instanceField) {
        if (instanceField == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }

        if (instanceField.isAnnotationPresent(Field.class) && !instanceField.getAnnotation(
                Field.class).value().isEmpty()) {
            return instanceField.getAnnotation(Field.class).value();
        }
        else {
            return toSnakeCase(instanceField.getName());
        }
    }

    /**
     * Gets the tag name for the specified instance field.
     * <p>
     * The tag name will be the value of the {@link Tag} annotation if present on the field,
     * otherwise it will be the instance field name converted to snake_case.
     *
     * @param instanceField the field to get the name for, not {@code null}
     *
     * @return the tag name
     *
     * @throws IllegalArgumentException if {@code instanceField} is {@code null}
     */
    String getTagName(java.lang.reflect.Field instanceField) {
        if (instanceField == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }

        if (instanceField.isAnnotationPresent(Tag.class) && !instanceField.getAnnotation(Tag.class)
                .value()
                .isEmpty()) {
            return instanceField.getAnnotation(Tag.class).value();
        }
        else {
            return toSnakeCase(instanceField.getName());
        }
    }

    private String toSnakeCase(String value) {
        StringBuilder sb = new StringBuilder();
        char[] charArray = value.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (i > 0 && Character.isUpperCase(c)) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
