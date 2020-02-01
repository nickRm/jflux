package com.github.nickrm.jflux.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class member should be written and read as a tag in InfluxDB.
 * <p>
 * Annotated classes can have zero or multiple members annotated as tags. Annotated class members
 * with {@code null} values are ignored.
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Tag {

    /**
     * What the tag is named in InfluxDB.
     * <p>
     * If this value is not set, then the tag name will be derived from the annotated class member's
     * name.
     *
     * @return the tag name
     */
    String value() default "";
}
