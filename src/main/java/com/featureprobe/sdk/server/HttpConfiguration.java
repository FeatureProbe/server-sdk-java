package com.featureprobe.sdk.server;

import okhttp3.ConnectionPool;
import okhttp3.Headers;

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

    final Headers headers;

    protected HttpConfiguration(Builder builder) {
        this.connectionPool = Builder.connectionPool;
        this.connectTimeout = Builder.connectTimeout;
        this.readTimeout = Builder.readTimeout;
        this.writeTimeout = Builder.writeTimeout;
        this.headers =  Builder.headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private static ConnectionPool connectionPool = DEFAULT_CONNECTION_POOL;

        private static Duration connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        private static Duration readTimeout = DEFAULT_READ_TIMEOUT;

        private static Duration writeTimeout = DEFAULT_WRITE_TIMEOUT;

        private static Headers headers = new Headers.Builder().build();

        public Builder() {
        }

        public Builder headers(Headers headers) {
            Builder.headers = headers == null ? new Headers.Builder().build() : headers;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            Builder.connectionPool = connectionPool == null ? DEFAULT_CONNECTION_POOL : connectionPool;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            Builder.connectTimeout = connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            Builder.readTimeout = readTimeout == null ? DEFAULT_READ_TIMEOUT : readTimeout;
            return this;
        }

        public Builder writeTimeout(Duration writeTimeout) {
            Builder.writeTimeout = writeTimeout == null ? DEFAULT_WRITE_TIMEOUT : writeTimeout;
            return this;
        }

        public HttpConfiguration build() {
            return new HttpConfiguration(this);
        }
    }
}
