package com.github.nickrm.jflux.annotation.exception;

import java.lang.annotation.Annotation;

/**
 * Thrown to indicate that an annotation which should only appear once, was found more than once.
 *
 * @since 1.0.0
 */
public final class DuplicateAnnotatedMembersException extends AnnotationProcessingException {

    /**
     * Constructs a new exception with the specified target object and annotation class.
     *
     * @param o               the object which caused the exception
     * @param annotationClass the annotation which was duplicated
     */
    public DuplicateAnnotatedMembersException(Object o,
            Class<? extends Annotation> annotationClass) {
        super(o + " has more than one members annotated with " + annotationClass.getName());
    }
}
