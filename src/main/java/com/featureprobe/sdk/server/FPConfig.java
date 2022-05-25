package com.featureprobe.sdk.server;

import java.net.URI;
import java.time.Duration;

public final class FPConfig {

    static final URI DEFAULT_REMOTE_URI = URI.create("http://127.0.0.1:4007");

    static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(5);

    protected static final FPConfig DEFAULT = new Builder().build();

    final Duration refreshInterval;

    final URI remoteUri;

    final String location;

    final SynchronizerFactory synchronizerFactory;

    final DataRepositoryFactory dataRepositoryFactory;

    final EventProcessorFactory eventProcessorFactory;

    final HttpConfiguration httpConfiguration;

    protected FPConfig(Builder builder) {
        this.refreshInterval = builder.refreshInterval;
        this.remoteUri = builder.remoteUri;
        this.location = builder.location;
        this.synchronizerFactory = builder.synchronizer == null ? new PollingSynchronizerFactory() :
                builder.synchronizer;
        this.dataRepositoryFactory = builder.dataRepository == null ? new MemoryDataRepositoryFactory() :
                builder.dataRepository;
        this.eventProcessorFactory = new DefaultEventProcessorFactory();
        this.httpConfiguration = builder.httpConfiguration == null ? HttpConfiguration.DEFAULT :
                builder.httpConfiguration;
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

        public FPConfig build() {
            return new FPConfig(this);
        }
    }
}
