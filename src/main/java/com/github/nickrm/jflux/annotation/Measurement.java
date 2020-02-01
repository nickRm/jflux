package com.github.nickrm.jflux.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that instances of a class can be serialized to/deserialized from InfluxDB.
 * <p>
 * Note that this annotation is optional and classes can still be (de-)serialized without it. It is
 * mainly a means to set the measurement name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Measurement {

    /**
     * What the measurement is named in InfluxDB.
     * <p>
     * If this value is not set, then the measurement name will be derived from the class name,
     *
     * @return the measurement name
     */
    String value() default "";
}
