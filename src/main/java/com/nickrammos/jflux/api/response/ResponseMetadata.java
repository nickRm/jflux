package com.nickrammos.jflux.api.response;

import java.time.Instant;

import com.nickrammos.jflux.domain.BuildType;
import com.nickrammos.jflux.domain.Version;

/**
 * Metadata relevant to an {@link ApiResponse}.
 *
 * @see Builder
 */
public final class ResponseMetadata {

    private final Instant timestamp;
    private final String requestId;
    private final BuildType dbBuildType;
    private final Version dbVersion;

    private ResponseMetadata(Builder builder) {
        this.timestamp = builder.timestamp;
        this.requestId = builder.requestId;
        this.dbBuildType = builder.dbBuildType;
        this.dbVersion = builder.dbVersion;
    }

    /**
     * Gets the timestamp of the response.
     *
     * @return the response timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the request ID for the response.
     *
     * @return the response request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets the build type of the InfluxDB instance.
     *
     * @return the database build type
     */
    public BuildType getDbBuildType() {
        return dbBuildType;
    }

    /**
     * Gets the version of the InfluxDB instance.
     *
     * @return the database version
     */
    public Version getDbVersion() {
        return dbVersion;
    }

    @Override
    public String toString() {
        return "ResponseMetadata{" + "timestamp=" + timestamp + ", requestId='" + requestId + '\''
                + ", dbBuildType=" + dbBuildType + ", dbVersion=" + dbVersion + '}';
    }

    /**
     * Used to build instances of {@link ResponseMetadata}.
     */
    public static final class Builder {

        private Instant timestamp;
        private String requestId;
        private BuildType dbBuildType;
        private Version dbVersion;

        /**
         * Sets the timestamp of the response.
         *
         * @param timestamp the response timestamp
         *
         * @return this builder
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Sets the request ID of the response
         *
         * @param requestId the response request ID
         *
         * @return this builder
         */
        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * Sets the build type of the InfluxDB instance.
         *
         * @param dbBuildType the database build type
         *
         * @return this builder
         */
        public Builder dbBuildType(BuildType dbBuildType) {
            this.dbBuildType = dbBuildType;
            return this;
        }

        /**
         * Sets the version of the InfluxDB instance.
         *
         * @param dbVersion the database version
         *
         * @return this builder
         */
        public Builder dbVersion(Version dbVersion) {
            this.dbVersion = dbVersion;
            return this;
        }

        /**
         * Builds a new {@link ResponseMetadata} instance with the values set in this builder.
         *
         * @return the newly built instance
         */
        public ResponseMetadata build() {
            return new ResponseMetadata(this);
        }
    }
}
