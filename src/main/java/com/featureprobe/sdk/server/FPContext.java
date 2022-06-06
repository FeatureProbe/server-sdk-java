package com.featureprobe.sdk.server;

import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

final class FPContext {

    private static final Logger logger = Loggers.MAIN;

    private static final String GET_REPOSITORY_DATA_API = "/api/server-sdk/toggles";

    private static final String POST_EVENTS_DATA_API = "/api/events";

    private  URL synchronizerUrl;

    private  URL eventUrl;

    private final String serverSdkKey;

    private final Duration refreshInterval;

    private final String location;

    private final HttpConfiguration httpConfiguration;

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
}
