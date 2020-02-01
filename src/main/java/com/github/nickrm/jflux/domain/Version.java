/*
 * Copyright 2019 Nick Rammos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.nickrm.jflux.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a version following semantic versioning schema.
 *
 * @see <a href="https://semver.org/">Semantic Versioning</a>
 */
public final class Version implements Comparable<Version> {

    private static final Pattern VALID_VERSION_PATTERN = Pattern.compile("v?\\d\\.\\d\\.\\d");

    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Creates a new instance setting the version numbers.
     *
     * @param major the major version
     * @param minor the minor version
     * @param patch the patch version
     *
     * @throws IllegalArgumentException if any of the version numbers are negative
     */
    Version(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version numbers cannot be negative");
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Creates a new instance from the specified string.
     * <p>
     * The input string must match the semantic versioning format and may start with a 'v'. E.g.
     * both following formats are valid:
     * <ul>
     * <li>1.0.0</li>
     * <li>v0.11.6</li>
     * </ul>
     * Whereas the following formats are invalid:
     * <ul>
     * <li>1.0</li>
     * <li>1.0.0-beta</li>
     * </ul>
     *
     * @param input the string to parse
     *
     * @return the new version instance
     *
     * @throws IllegalArgumentException if the input string is not in the correct format
     */
    public static Version fromString(String input) {
        if (!VALID_VERSION_PATTERN.matcher(input).matches()) {
            throw new IllegalArgumentException("Invalid version format: " + input);
        }

        if (input.startsWith("v")) {
            input = input.substring(1);
        }

        String[] versionParts = input.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        int patch = Integer.parseInt(versionParts[2]);

        return new Version(major, minor, patch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public int compareTo(Version other) {
        if (other == null) {
            throw new NullPointerException();
        }

        if (this.equals(other)) {
            return 0;
        }

        if (major != other.major) {
            return Integer.compare(major, other.major);
        }

        if (minor != other.minor) {
            return Integer.compare(minor, other.minor);
        }

        return Integer.compare(patch, other.patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
