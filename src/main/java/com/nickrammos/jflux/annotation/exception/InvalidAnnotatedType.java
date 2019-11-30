package com.nickrammos.jflux.annotation.exception;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.nickrammos.jflux.annotation.Timestamp;

/**
 * Thrown to indicate that an annotation was applied to a member of a type incompatible with that
 * annotation (e.g. an integer field annotated with {@link Timestamp}).
 */
public final class InvalidAnnotatedType extends AnnotationProcessingException {

    /**
     * Constructs a new exception for the specified class member and type.
     *
     * @param member       the class member with the wrong type
     * @param expectedType the type that the annotation is valid for
     */
    public InvalidAnnotatedType(Member member, Class<?> expectedType) {
        this(member, Collections.singleton(expectedType));
    }

    /**
     * Constructs a new exception for the specified class member and types.
     *
     * @param member        the class member with the wrong type
     * @param expectedTypes the types that the annotation is valid for
     */
    public InvalidAnnotatedType(Member member, Collection<Class<?>> expectedTypes) {
        super(constructMessage(member, expectedTypes));
    }

    private static String constructMessage(Member member, Collection<Class<?>> expectedTypes) {
        return member.getDeclaringClass().getName() + "." + member.getName()
                + " type is expected to be one of " + expectedTypes.stream()
                .map(Class::getName)
                .collect(Collectors.toList());
    }
}
