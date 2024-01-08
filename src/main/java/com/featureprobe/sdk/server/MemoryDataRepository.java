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

import com.featureprobe.sdk.server.model.Repository;
import com.featureprobe.sdk.server.model.Segment;
import com.featureprobe.sdk.server.model.Toggle;
import com.google.common.collect.ImmutableMap;

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
            if (Objects.nonNull(data)
                    && Objects.nonNull(data.getVersion())
                    && Objects.nonNull(repository.getVersion())
                    && data.getVersion() >= repository.getVersion()) {
                return;
            }
            Map<String, Toggle> toggles = ImmutableMap.copyOf(repository.getToggles());
            Map<String, Segment> segments = ImmutableMap.copyOf(repository.getSegments());
            data = new Repository(toggles, segments, repository.getDebugUntilTime(), repository.getVersion());
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
    public Long getDebugUntilTime() {
        return data.getDebugUntilTime();
    }

    @Override
    public boolean initialized() {
        return this.initialized;
    }

    @Override
    public void close() {
        data = null;
    }
}
