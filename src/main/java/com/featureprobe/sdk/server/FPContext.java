package com.featureprobe.sdk.server;

import okhttp3.Headers;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

final class FPContext {

    private static final Logger logger = Loggers.MAIN;

    private static final String GET_SDK_KEY_HEADER = "Authorization";

    private static final String USER_AGENT_HEADER = "user-agent";

    private static final String DEFAULT_SDK_VERSION = "unknown";

    private static final String SDK_FLAG_PREFIX = "Java/";

    private static final String GET_REPOSITORY_DATA_API = "/api/server-sdk/toggles";

    private static final String POST_EVENTS_DATA_API = "/api/events";

    private static final String REALTIME_TOGGLE_UPDATE_API = "/realtime";

    private  URL synchronizerUrl;

    private  URL eventUrl;

    private URI realtimeUri;

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
            if (Objects.isNull(config.realtimeUri)) {
                this.realtimeUri = URI.create(config.remoteUri.toString() + REALTIME_TOGGLE_UPDATE_API);
            } else {
                this.realtimeUri = config.realtimeUri;
            }
        } catch (MalformedURLException e) {
            logger.error("construction context error", e);
        }
        this.serverSdkKey = serverSdkKey;
        this.refreshInterval = config.refreshInterval;
        this.location = config.location;
        this.httpConfiguration = config.httpConfiguration;
        String sdkVersion = getVersion();
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

    public URI getRealtimeUri() {
        return realtimeUri;
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

    private synchronized String getVersion() {
        String version = null;
        try {
            Properties p = new Properties();
            InputStream is = getClass()
                    .getResourceAsStream("/META-INF/maven/com.featureprobe/server-sdk-java/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "");
            }
        } catch (Exception e) {

        }
        if (version == null) {
            Package aPackage = getClass().getPackage();
            if (aPackage != null) {
                version = aPackage.getImplementationVersion();
                if (version == null) {
                    version = aPackage.getSpecificationVersion();
                }
            }
        }
        if (version == null) {
            version = "";
        }
        return version;
    }
}
