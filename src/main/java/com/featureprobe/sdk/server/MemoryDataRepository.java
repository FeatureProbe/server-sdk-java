package com.featureprobe.sdk.server;

import com.featureprobe.sdk.server.model.Repository;
import com.featureprobe.sdk.server.model.Segment;
import com.featureprobe.sdk.server.model.Toggle;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

final class MemoryDataRepository implements DataRepository {

    private volatile Repository data;

    private volatile boolean initialized = false;

    private volatile Long updatedTimestamp = 0L;

    @Override
    public void refresh(Repository repository) {

        if (Objects.nonNull(repository) && Objects.nonNull(repository.getToggles())
                && Objects.nonNull(repository.getSegments())) {
            Map<String, Toggle> toggles = ImmutableMap.copyOf(repository.getToggles());
            Map<String, Segment> segments = ImmutableMap.copyOf(repository.getSegments());
            data = new Repository(toggles, segments);
            this.initialized = true;
            this.updatedTimestamp = System.currentTimeMillis();
        }

    }

    @Override
    public Toggle getToggle(String key) {
        if (initialized) {
            return data.getToggles().get(key);
        }
        return null;
    }

    @Override
    public Map<String, Toggle> getAllToggle() {
        if (initialized) {
            return data.getToggles();
        }
        return ImmutableMap.of();
    }

    @Override
    public Segment getSegment(String key) {
        if (initialized) {
            return data.getSegments().get(key);
        }
        return null;
    }

    @Override
    public Map<String, Segment> getAllSegment() {
        if (initialized) {
            return data.getSegments();
        }
        return ImmutableMap.of();
    }

    @Override
    public boolean initialized() {
        return this.initialized;
    }

    @Override
    public void close() {
        data = null;
        return;
    }
}
