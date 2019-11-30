package com.nickrammos.jflux;

import com.nickrammos.jflux.annotation.Field;
import com.nickrammos.jflux.annotation.Measurement;
import com.nickrammos.jflux.annotation.Tag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class NamingStrategyTest {

    private static final String TEST_MEASUREMENT_NAME = "test_measurement";
    private static final String TEST_FIELD_NAME = "some_field";
    private static final String TEST_TAG_NAME = "some_tag";

    private final NamingStrategy namingStrategy = new NamingStrategy();

    @Test
    public void getMeasurement_shouldReturnAnnotationValue_ifPresent() {
        String actualMeasurementName = namingStrategy.getMeasurementName(AnnotatedClass.class);
        assertThat(actualMeasurementName).isEqualTo(TEST_MEASUREMENT_NAME);
    }

    @Test
    public void getMeasurement_shouldReturnClassNameInSnakeCase_ifAnnotationNotPresent() {
        String measurementName = namingStrategy.getMeasurementName(NonAnnotatedClass.class);
        assertThat(measurementName).isEqualTo("non_annotated_class");
    }

    @Test
    public void getMeasurement_shouldReturnClassNameInSnakeCase_ifAnnotationValueIsEmpty() {
        String measurementName = namingStrategy.getMeasurementName(AnnotatedWithNoValueClass.class);
        assertThat(measurementName).isEqualTo("annotated_with_no_value_class");
    }

    @Test
    public void getMeasurementName_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> namingStrategy.getMeasurementName(null));
    }

    @Test
    public void getFieldName_shouldReturnAnnotationValue_ifPresent() throws NoSuchFieldException {
        String fieldName =
                namingStrategy.getFieldName(AnnotatedClass.class.getDeclaredField("field"));
        assertThat(fieldName).isEqualTo(TEST_FIELD_NAME);
    }

    @Test
    public void getFieldName_shouldReturnFieldNameInSnakeCase_ifAnnotationValueIsEmpty()
            throws NoSuchFieldException {
        String fieldName = namingStrategy.getFieldName(
                AnnotatedWithNoValueClass.class.getDeclaredField("annotatedField"));
        assertThat(fieldName).isEqualTo("annotated_field");
    }

    @Test
    public void getFieldName_shouldReturnFieldNameInSnakeCase_ifAnnotationNotPresent()
            throws NoSuchFieldException {
        String fieldName = namingStrategy.getFieldName(
                NonAnnotatedClass.class.getDeclaredField("nonAnnotatedField"));
        assertThat(fieldName).isEqualTo("non_annotated_field");
    }

    @Test
    public void getFieldName_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> namingStrategy.getFieldName(null));
    }

    @Test
    public void getTagName_shouldReturnAnnotationValue_ifPresent() throws NoSuchFieldException {
        String tagName = namingStrategy.getTagName(
                AnnotatedClass.class.getDeclaredField("tag"));
        assertThat(tagName).isEqualTo(TEST_TAG_NAME);
    }

    @Test
    public void getTagName_shouldReturnFieldNameInSnakeCase_ifAnnotationValueIsEmpty()
            throws NoSuchFieldException {
        String tagName = namingStrategy.getTagName(
                AnnotatedWithNoValueClass.class.getDeclaredField("annotatedTag"));
        assertThat(tagName).isEqualTo("annotated_tag");
    }

    @Test
    public void getTagName_shouldReturnFieldNameInSnakeCase_ifAnnotationNotPresent()
            throws NoSuchFieldException {
        String tagName = namingStrategy.getTagName(
                NonAnnotatedClass.class.getDeclaredField("nonAnnotatedTag"));
        assertThat(tagName).isEqualTo("non_annotated_tag");
    }

    @Test
    public void getTagName_shouldThrowException_ifInputIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> namingStrategy.getTagName(null));
    }

    @Measurement(TEST_MEASUREMENT_NAME)
    private static class AnnotatedClass {

        @Field(TEST_FIELD_NAME)
        private int field;

        @Tag(TEST_TAG_NAME)
        private String tag;
    }

    @Measurement
    private static class AnnotatedWithNoValueClass {

        @Field
        private int annotatedField;

        @Tag
        private String annotatedTag;
    }

    private static class NonAnnotatedClass {

        private int nonAnnotatedField;

        private String nonAnnotatedTag;
    }
}