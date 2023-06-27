/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.featureprobe.sdk.server;

import okhttp3.Headers;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

class FPContext {

    private static final Logger logger = Loggers.MAIN;

    private static final String GET_SDK_KEY_HEADER = "Authorization";

    private static final String USER_AGENT_HEADER = "user-agent";

    private static final String DEFAULT_SDK_VERSION = "unknown";

    private static final String SDK_FLAG_PREFIX = "Java/";

    private static final String GET_REPOSITORY_DATA_API = "/api/server-sdk/toggles";

    private static final String POST_EVENTS_DATA_API = "/api/events";

    private static final String REALTIME_URI_PATH = "/realtime";

    private URL synchronizerUrl;

    private URI realtimeUri;

    private URL eventUrl;

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
                this.realtimeUri = new URI(config.remoteUri.toString() + REALTIME_URI_PATH);
            } else {
                this.realtimeUri = config.realtimeUri;
            }
        } catch (MalformedURLException e) {
            logger.error("construction context error MalformedURLException");
        } catch (URISyntaxException e) {
            logger.error("construction context error URISyntaxException");
        }
        this.serverSdkKey = serverSdkKey;
        this.refreshInterval = config.refreshInterval;
        this.location = config.location;
        this.httpConfiguration = config.httpConfiguration;
        String sdkVersion = getVersion();
        this.headers = config.httpConfiguration.headers.newBuilder().add(GET_SDK_KEY_HEADER, serverSdkKey)
                .add(USER_AGENT_HEADER, SDK_FLAG_PREFIX + sdkVersion).build();
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

    public synchronized String getVersion() {
        try (
                InputStream is =
                        getResourceAsStream("/META-INF/maven/com.featureprobe/server-sdk-java/pom.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                return p.getProperty("version", DEFAULT_SDK_VERSION);
            }
        } catch (IOException e) {
            logger.error("get version error", e);
        }

        Package aPackage = getaPackage();
        if (aPackage == null) {
            return DEFAULT_SDK_VERSION;
        }

        String version = aPackage.getImplementationVersion();
        if (version != null) {
            return version;
        }
        version = aPackage.getSpecificationVersion();
        if (version != null) {
            return version;
        }
        return DEFAULT_SDK_VERSION;
    }

    protected Package getaPackage() {
        return getClass().getPackage();
    }

    protected InputStream getResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    public URI getRealtimeUri() {
        return realtimeUri;
    }
}
