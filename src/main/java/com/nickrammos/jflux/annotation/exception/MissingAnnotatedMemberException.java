package com.nickrammos.jflux.annotation.exception;

import java.lang.annotation.Annotation;

/**
 * Thrown to indicate that a required annotation is missing.
 */
public final class MissingAnnotatedMemberException extends AnnotationProcessingException {

    /**
     * Constructs a new exception with the specified target object and annotation type.
     *
     * @param o               the object which caused the exception
     * @param annotationClass the annotation which was missing on the object
     */
    public MissingAnnotatedMemberException(Object o, Class<? extends Annotation> annotationClass) {
        super(o + " has no members annotated with " + annotationClass.getName());
    }
}
