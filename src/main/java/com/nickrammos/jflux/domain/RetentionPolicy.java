package com.nickrammos.jflux.domain;

import java.time.Duration;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * A retention policy specifies how long data is kept for.
 * <p>
 * Instance of this class are immutable.
 */
public final class RetentionPolicy {

    private final String name;

    /**
     * The duration of this retention policy, cannot be {@code null} or negative. A duration of
     * {@link Duration#ZERO} implies an infinite duration.
     */
    private final Duration duration;

    /**
     * The replication factor for this retention policy, cannot be negative. Replication has no
     * meaning in single-node setups.
     */
    private final int replication;

    /**
     * The shard group duration of this retention policy, cannot be {@code null} or negative. A
     * shard duration of {@link Duration#ZERO} means that the default shard duration will be used
     * (the default value is based on the value of {@link #duration}).
     */
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
     * Gets the {@link #duration} of this retention policy.
     *
     * @return the retention policy duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Creates a copy of this instance with the specified {@link #duration}.
     *
     * @param duration the new duration value
     *
     * @return a new instance with the new value
     */
    public RetentionPolicy withDuration(Duration duration) {
        return new Builder(this).duration(duration).build();
    }

    /**
     * Gets the {@link #replication replication factor} of this retention policy.
     *
     * @return the retention policy replication
     */
    public int getReplication() {
        return replication;
    }

    /**
     * Creates a copy of this instance with the specified {@link #replication replication factor}.
     *
     * @param replication the new replication value
     *
     * @return a new instance with the new value
     */
    public RetentionPolicy withReplication(int replication) {
        return new Builder(this).replication(replication).build();
    }

    /**
     * Gets the {@link #shardDuration shard duration} of this retention policy.
     *
     * @return the shard duration
     */
    public Duration getShardDuration() {
        return shardDuration;
    }

    /**
     * Creates a copy of this instance with a new {@link #shardDuration shard duration}.
     *
     * @param shardDuration the new shard duration value
     *
     * @return a new instance with the new value
     */
    public RetentionPolicy withShardDuration(Duration shardDuration) {
        return new Builder(this).shardDuration(shardDuration).build();
    }

    /**
     * Gets a value indicating whether this retention policy is the default.
     *
     * @return {@code true} if this is the default, {@code false} otherwise
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Creates a copy of this instance with a new value for {@link #isDefault}.
     *
     * @param isDefault {@code true} if this retention policy is the default, {@code false}
     *                  otherwise
     *
     * @return a new instance with the new value
     */
    public RetentionPolicy asDefault(boolean isDefault) {
        return new Builder(this).isDefault(isDefault).build();
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
         * <p>
         * The fields specified in this constructor are mandatory, the rest are optional and if not
         * set will fall back to default values.
         *
         * @param name     name of the retention policy
         * @param duration duration of the retention policy
         *
         * @see RetentionPolicy#duration
         */
        public Builder(String name, Duration duration) {
            this.name = name;
            this.duration = duration;
            replication = 1;
            shardDuration = Duration.ZERO;
            isDefault = false;
        }

        private Builder(RetentionPolicy retentionPolicy) {
            this.name = retentionPolicy.name;
            this.duration = retentionPolicy.duration;
            this.replication = retentionPolicy.replication;
            this.shardDuration = retentionPolicy.shardDuration;
            this.isDefault = retentionPolicy.isDefault;
        }

        /**
         * Sets the {@link RetentionPolicy#duration} for the retention policy.
         *
         * @param duration the retention policy duration
         *
         * @return this builder
         */
        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Sets the {@link RetentionPolicy#replication replication factor} for the retention policy.
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
         * Sets the {@link RetentionPolicy#shardDuration shard duration} for the retention policy.
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
