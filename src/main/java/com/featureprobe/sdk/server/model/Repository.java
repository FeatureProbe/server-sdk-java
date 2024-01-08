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

package com.featureprobe.sdk.server.model;

import java.util.Map;

public final class Repository {

    private Map<String, Toggle> toggles;

    private Map<String, Segment> segments;

    private Long debugUntilTime;

    private Long version;

    public Repository() {
    }

    public Repository(Map<String, Toggle> toggles, Map<String, Segment> segments, Long debugUntilTime, Long version) {
        this.toggles = toggles;
        this.segments = segments;
        this.debugUntilTime = debugUntilTime;
        this.version = version;
    }

    public Map<String, Toggle> getToggles() {
        return toggles;
    }

    public void setToggles(Map<String, Toggle> toggles) {
        this.toggles = toggles;
    }

    public Map<String, Segment> getSegments() {
        return segments;
    }

    public void setSegments(Map<String, Segment> segments) {
        this.segments = segments;
    }

    public Long getDebugUntilTime() {
        return debugUntilTime;
    }

    public void setDebugUntilTime(Long debugUntilTime) {
        this.debugUntilTime = debugUntilTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
