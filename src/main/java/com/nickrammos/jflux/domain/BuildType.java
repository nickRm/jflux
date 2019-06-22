package com.nickrammos.jflux.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The build type of an InfluxDB instance.
 */
public enum BuildType {

    OPEN_SOURCE("OSS", "OSS"),
    ENTERPRISE("ENT", "Enterprise");

    private static final Map<String, BuildType> LOOKUP_MAP =
            Arrays.stream(BuildType.values()).collect(
                    Collectors.toMap(BuildType::getIdentifier, Function.identity()));

    private final String identifier;
    private final String name;

    BuildType(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    /**
     * Gets the shorthand identifier of this build type.
     *
     * @return the build type identifier
     */
    String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the build type with the specified shorthand identifier.
     *
     * @param identifier the identifier to search for
     *
     * @return the build type that corresponds to the identifier
     *
     * @throws IllegalArgumentException if no build type with the specified identifier exists
     */
    public static BuildType fromIdentifier(String identifier) {
        if (LOOKUP_MAP.containsKey(identifier)) {
            return LOOKUP_MAP.get(identifier);
        }
        else {
            throw new IllegalArgumentException("Invalid identifier " + identifier);
        }
    }
}
