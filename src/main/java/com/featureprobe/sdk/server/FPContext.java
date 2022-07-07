package com.featureprobe.sdk.server;

import okhttp3.Headers;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

final class FPContext {

    private static final Logger logger = Loggers.MAIN;

    private static final String GET_SDK_KEY_HEADER = "Authorization";

    private static final String USER_AGENT_HEADER = "user-agent";

    private static final String DEFAULT_SDK_VERSION = "unknown";

    private static final String SDK_FLAG_PREFIX = "Java/";

    private static final String GET_REPOSITORY_DATA_API = "/api/server-sdk/toggles";

    private static final String POST_EVENTS_DATA_API = "/api/events";

    private  URL synchronizerUrl;

    private  URL eventUrl;

    private final String serverSdkKey;

    private final Duration refreshInterval;

    private final String location;

    private final HttpConfiguration httpConfiguration;

    private final Headers headers;

    FPContext(String serverSdkKey, FPConfig config) {
        try {
            if (Objects.isNull(config.synchronizerUrl)) {
                this.synchronizerUrl = new URL(config.remoteUri.toString() + GET_REPOSITORY_DATA_API);
            } else {
                this.synchronizerUrl = config.synchronizerUrl;
            }
            if (Objects.isNull(config.eventUrl)) {
                this.eventUrl = new URL(config.remoteUri.toString() + POST_EVENTS_DATA_API);
            } else {
                this.eventUrl = config.eventUrl;
            }
        } catch (MalformedURLException e) {
            logger.error("construction context error", e);
        }
        this.serverSdkKey = serverSdkKey;
        this.refreshInterval = config.refreshInterval;
        this.location = config.location;
        this.httpConfiguration = config.httpConfiguration;
        String sdkVersion = DEFAULT_SDK_VERSION;
        try {
            sdkVersion = Utils.readSdkVersion();
        } catch (IOException e) {
            logger.error("read sdk version error", e);
        }
        this.headers = config.httpConfiguration.headers.newBuilder()
                .add(GET_SDK_KEY_HEADER, serverSdkKey)
                .add(USER_AGENT_HEADER, SDK_FLAG_PREFIX + sdkVersion)
                .build();
    }

    public URL getSynchronizerUrl() {
        return synchronizerUrl;
    }

    public URL getEventUrl() {
        return eventUrl;
    }

    public String getServerSdkKey() {
        return serverSdkKey;
    }

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public String getLocation() {
        return location;
    }

    public HttpConfiguration getHttpConfiguration() {
        return httpConfiguration;
    }

    public Headers getHeaders() {
        return headers;
    }
}
