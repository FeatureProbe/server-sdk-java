package com.featureprobe.sdk.server;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

final class FPContext {

    private final URI remoteUri;

    private final String serverSdkKey;

    private final Duration refreshInterval;

    private final String location;

    private final HttpConfiguration httpConfiguration;

    FPContext(String serverSdkKey, FPConfig config) {
        this.remoteUri = config.remoteUri;
        this.serverSdkKey = serverSdkKey;
        this.refreshInterval = config.refreshInterval;
        this.location = config.location;
        this.httpConfiguration = config.httpConfiguration;
    }

    public URI getRemoteUri() {
        return remoteUri;
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
