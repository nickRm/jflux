package com.nickrammos.jflux.domain;

import java.time.Duration;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * A retention policy specifies how long data is kept for.
 */
public final class RetentionPolicy {

    private final String name;
    private final Duration duration;
    private final int replication;
    private final Duration shardDuration;
    private boolean isDefault;

    /**
     * Instances can only be created by {@link Builder}.
     *
     * @param builder holds the values used to initialize this instance
     *
     * @throws IllegalStateException if any of the values set in the builder are illegal
     */
    private RetentionPolicy(Builder builder) {
        this.name = builder.name;
        this.duration = builder.duration;
        this.replication = builder.replication;
        this.shardDuration = builder.shardDuration;
        this.isDefault = builder.isDefault;

        if (StringUtils.isBlank(this.name)) {
            throw new IllegalStateException("Retention policy name cannot be blank");
        }

        if (this.duration == null || this.duration.isNegative()) {
            throw new IllegalStateException("Duration cannot be null or negative");
        }

        if (this.replication < 1) {
            throw new IllegalStateException("Replication cannot be negative");
        }

        if (this.shardDuration == null || shardDuration.isNegative()) {
            throw new IllegalStateException("Shard duration cannot be null or negative");
        }
    }

    /**
     * Gets the name of this retention policy.
     *
     * @return the retention policy name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the duration of this retention policy.
     * <p>
     * A value of {@link Duration#ZERO} indicates an infinite duration.
     *
     * @return the retention policy duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Gets the replication factor of this retention policy.
     *
     * @return the retention policy replication
     */
    public int getReplication() {
        return replication;
    }

    /**
     * Gets the shard duration of this retention policy.
     * <p>
     * A value of {@link Duration#ZERO} indicates a default shard duration based on the value of the
     * retention policy's duration.
     *
     * @return the shard duration
     */
    public Duration getShardDuration() {
        return shardDuration;
    }

    /**
     * Gets a value indiciating whether this retention policy is the default.
     *
     * @return {@code true} if this is the default, {@code false} otherwise
     */
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RetentionPolicy that = (RetentionPolicy) o;
        return replication == that.replication && isDefault == that.isDefault && name.equals(
                that.name) && duration.equals(that.duration) && Objects.equals(shardDuration,
                that.shardDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, duration, replication, shardDuration, isDefault);
    }

    @Override
    public String toString() {
        return "RetentionPolicy{" + "name='" + name + '\'' + ", duration=" + duration
                + ", replication=" + replication + ", shardDuration=" + shardDuration
                + ", isDefault=" + isDefault + '}';
    }

    /**
     * Used to create instances of {@link RetentionPolicy}.
     */
    public static final class Builder {

        private String name;
        private Duration duration;
        private int replication;
        private Duration shardDuration;
        private boolean isDefault;

        /**
         * Initializes a new instance setting the required fields.
         *
         * @param name     name of the retention policy
         * @param duration duration of the retention policy, {@link Duration#ZERO} is infinite
         *                 duration
         */
        public Builder(String name, Duration duration) {
            this.name = name;
            this.duration = duration;
            replication = 1;
            shardDuration = Duration.ZERO;
            isDefault = false;
        }

        /**
         * Sets the replication for the retention policy.
         * <p>
         * If not set, a default value of 1 will be used. This value has no meaning in single-node
         * clusters.
         *
         * @param replication number of replicas, must be positive
         *
         * @return this builder
         */
        public Builder replication(int replication) {
            this.replication = replication;
            return this;
        }

        /**
         * Sets the shard duration for the retention policy.
         * <p>
         * If not set or if set to {@link Duration#ZERO}, this defaults to a default value based on
         * the retention policy duration.
         *
         * @param shardDuration the shard duration to use, must not be {@code null} or negative
         *
         * @return this builder
         */
        public Builder shardDuration(Duration shardDuration) {
            this.shardDuration = shardDuration;
            return this;
        }

        /**
         * Sets a value indicating whether this retention policy should be the default retention
         * policy.
         * <p>
         * If not set, this value defaults to {@code false}.
         *
         * @param isDefault whether the retention policy is the default one
         *
         * @return this builder
         */
        public Builder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        /**
         * Constructs a retention policy instance from the values set in this builder.
         *
         * @return the new retention policy instance
         *
         * @throws IllegalStateException if any of the values set in this builder are illegal
         */
        public RetentionPolicy build() {
            return new RetentionPolicy(this);
        }
    }
}
