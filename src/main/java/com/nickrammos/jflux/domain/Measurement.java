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

package com.nickrammos.jflux.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A container of {@link Point Points}, roughly equivalent to a relational DB table.
 *
 * @see Builder
 */
public final class Measurement {

    private final String name;
    private final List<Point> points;

    /**
     * Instances of this class can only be constructed using {@link Builder}.
     *
     * @param builder used to construct the instance
     */
    private Measurement(Builder builder) {
        name = builder.name;
        points = builder.points;
    }

    /**
     * Gets the name of this measurement.
     *
     * @return the measurement name, or {@code null} if not available
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the points in this measurement.
     *
     * @return the measurement's points, or an empty list if none
     */
    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    @Override
    public String toString() {
        return "Measurement{" + "name='" + name + '\'' + ", points=" + points + '}';
    }

    /**
     * Creates instances of {@link Measurement}.
     */
    public static final class Builder {

        private String name;
        private List<Point> points = Collections.emptyList();

        /**
         * Sets the name for the measurement to be constructed.
         *
         * @param name the measurement name
         *
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the points for the measurement to be constructed.
         *
         * @param points the measurement's points
         *
         * @return this builder
         */
        public Builder points(List<Point> points) {
            this.points = points;
            return this;
        }

        /**
         * Creates a new {@link Measurement} instance using the values in this builder.
         *
         * @return the constructed {@link Measurement}
         */
        public Measurement build() {
            return new Measurement(this);
        }
    }
}
