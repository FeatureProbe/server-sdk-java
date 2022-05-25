package com.featureprobe.sdk.server;

import okhttp3.ConnectionPool;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class HttpConfiguration {

    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofMillis(3000);

    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofMillis(3000);

    static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofMillis(3000);

    static final ConnectionPool DEFAULT_CONNECTION_POOL =
            new ConnectionPool(5, 5, TimeUnit.SECONDS);

    protected static final HttpConfiguration DEFAULT = new Builder().build();

    final ConnectionPool connectionPool;

    final Duration connectTimeout;

    final Duration readTimeout;

    final Duration writeTimeout;

    protected HttpConfiguration(Builder builder) {
        this.connectionPool = builder.connectionPool;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static ConnectionPool connectionPool = DEFAULT_CONNECTION_POOL;

        private static Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        private static Duration readTimeout = DEFAULT_READ_TIMEOUT;

        private static Duration writeTimeout = DEFAULT_WRITE_TIMEOUT;

        public Builder() {
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool == null ? DEFAULT_CONNECTION_POOL : connectionPool;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout == null ? DEFAULT_READ_TIMEOUT : readTimeout;
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout == null ? DEFAULT_WRITE_TIMEOUT : writeTimeout;
            return this;
        }

        public HttpConfiguration build() {
            return new HttpConfiguration(this);
        }
    }
}
