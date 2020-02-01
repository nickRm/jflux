package com.github.nickrm.jflux.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class member should be written and read as a field in InfluxDB.
 * <p>
 * Annotated classes must have at least one member annotated as a field. Annotated members with
 * {@code null} values are ignored. Note that only numeric and boolean types can be fields.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Field {

    /**
     * What the field is named in InfluxDB.
     * <p>
     * If this value is not set, then the field name will be derived from the annotated class
     * member's name.
     *
     * @return the field name
     */
    String value() default "";
}
