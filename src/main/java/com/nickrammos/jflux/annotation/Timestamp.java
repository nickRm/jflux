package com.nickrammos.jflux.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class member should be written and read as the point timestamp in InfluxDB.
 * <p>
 * Note that only fields of type {@link java.time.Instant} can be timestamps, and that there can
 * only be one timestamp per class. If no timestamp is defined in a class, then timestamp will be
 * set by InfluxDB when writing the point.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Timestamp {

}
