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

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class FPConfig {

    static final URI DEFAULT_REMOTE_URI = URI.create("http://localhost:4009/server");

    static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(5);

    static final Duration DEFAULT_REALTIME_INTERVAL = Duration.ofSeconds(10);

    static final Long DEFAULT_START_WAIT = TimeUnit.SECONDS.toNanos(5);

    protected static final FPConfig DEFAULT = new Builder().build();

    final Duration refreshInterval;

    final Long startWait;

    final URI remoteUri;

    URL synchronizerUrl;

    URL eventUrl;

    URI realtimeUri;

    final String location;

    final SynchronizerFactory synchronizerFactory;

    final DataRepositoryFactory dataRepositoryFactory;

    final EventProcessorFactory eventProcessorFactory;

    final HttpConfiguration httpConfiguration;

    protected FPConfig(Builder builder) {
        this.refreshInterval = builder.refreshInterval;
        this.remoteUri = builder.remoteUri;
        this.location = builder.location;
        this.synchronizerFactory =
                builder.synchronizer == null ? new StreamingSynchronizerFactory() : builder.synchronizer;
        this.dataRepositoryFactory =
                builder.dataRepository == null ? new MemoryDataRepositoryFactory() : builder.dataRepository;
        this.eventProcessorFactory = new DefaultEventProcessorFactory();
        this.httpConfiguration =
                builder.httpConfiguration == null ? HttpConfiguration.DEFAULT : builder.httpConfiguration;
        this.synchronizerUrl = builder.synchronizerUrl;
        this.eventUrl = builder.eventUrl;
        this.realtimeUri = builder.realtimeUri;
        this.startWait = builder.startWait == null ? DEFAULT_START_WAIT : builder.startWait;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Duration refreshInterval = DEFAULT_INTERVAL;

        private URI remoteUri = DEFAULT_REMOTE_URI;

        private String location;

        private SynchronizerFactory synchronizer;

        private DataRepositoryFactory dataRepository;

        private HttpConfiguration httpConfiguration;

        private URL synchronizerUrl;

        private URL eventUrl;

        private URI realtimeUri;

        private Long startWait;

        public Builder() {
        }

        public Builder remoteUri(String remoteUri) {
            this.remoteUri = remoteUri == null ? DEFAULT_REMOTE_URI : URI.create(remoteUri);
            return this;
        }

        public Builder pollingMode() {
            this.synchronizer = new PollingSynchronizerFactory();
            return this;
        }

        public Builder streamingMode() {
            this.refreshInterval = DEFAULT_REALTIME_INTERVAL;
            this.synchronizer = new StreamingSynchronizerFactory();
            return this;
        }

        public Builder streamingMode(Duration refreshInterval) {
            this.refreshInterval = refreshInterval;
            this.synchronizer = new StreamingSynchronizerFactory();
            return this;
        }

        public Builder pollingMode(Duration refreshInterval) {
            this.refreshInterval = refreshInterval;
            this.synchronizer = new PollingSynchronizerFactory();
            return this;
        }

        public Builder pollingMode(HttpConfiguration httpConfiguration) {
            this.httpConfiguration = httpConfiguration;
            this.synchronizer = new PollingSynchronizerFactory();
            return this;
        }

        public Builder localFileMode() {
            this.synchronizer = new FileSynchronizerFactory();
            return this;
        }

        public Builder localFileMode(String location) {
            this.location = location;
            this.synchronizer = new FileSynchronizerFactory();
            return this;
        }

        public Builder useMemoryRepository() {
            this.dataRepository = new MemoryDataRepositoryFactory();
            return this;
        }

        public Builder synchronizerUrl(URL synchronizerUrl) {
            this.synchronizerUrl = synchronizerUrl;
            return this;
        }

        public Builder eventUrl(URL eventUrl) {
            this.eventUrl = eventUrl;
            return this;
        }

        public Builder realtimeUri(URI realtimeUri) {
            this.realtimeUri = realtimeUri;
            return this;
        }

        public Builder realtimeUri(String realtimeUri) {
            this.realtimeUri = URI.create(realtimeUri);
            return this;
        }

        public Builder startWait(Long startWaitTime, TimeUnit unit) {
            this.startWait = unit.toNanos(startWaitTime);
            return this;
        }

        public FPConfig build() {
            return new FPConfig(this);
        }
    }
}
