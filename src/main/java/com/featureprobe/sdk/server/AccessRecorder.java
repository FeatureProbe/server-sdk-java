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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessRecorder {

    Map<String, List<Counter>> counters;

    long startTime;

    long endTime;

    AccessRecorder() {
        counters = new HashMap<>();
    }

    private AccessRecorder(AccessRecorder accessRecorder) {
        counters = new HashMap<>(accessRecorder.counters);
        startTime = accessRecorder.startTime;
        endTime = System.currentTimeMillis();
    }

    static final class Counter {

        long count;

        final String value;

        final Long version;

        final Integer index;

        public Counter(String value, Long version, Integer index) {
            this.value = value;
            this.version = version;
            this.index = index;
            count = 1L;
        }

        public void increment() {
            ++count;
        }

        public boolean isGroup(String value, Long version, Integer index) {
            return this.value.equals(value) && this.version.equals(version) && this.index.equals(index);
        }

        public long getCount() {
            return count;
        }

        public String getValue() {
            return value;
        }

        public Long getVersion() {
            return version;
        }

        public Integer getIndex() {
            return index;
        }
    }

    public void add(Event event) {
        AccessEvent accessEvent = (AccessEvent) event;
        if (counters.isEmpty()) {
            startTime = System.currentTimeMillis();
        }
        if (counters.containsKey(accessEvent.getKey())) {
            List<Counter> counters = this.counters.get(accessEvent.getKey());
            for (Counter counter : counters) {
                if (counter.isGroup(accessEvent.getValue(), accessEvent.getVersion(), accessEvent.getIndex())) {
                    counter.increment();
                    return;
                }
            }
            counters.add(new Counter(accessEvent.getValue(), accessEvent.getVersion(), accessEvent.getIndex()));
        } else {
            List<Counter> groups = new ArrayList<>(1);
            groups.add(new Counter(accessEvent.getValue(), accessEvent.getVersion(), accessEvent.getIndex()));
            counters.put(accessEvent.getKey(), groups);
        }
    }

    public void clear() {
        counters = new HashMap<>();
    }

    public AccessRecorder snapshot() {
        return new AccessRecorder(this);
    }

    public Map<String, List<Counter>> getCounters() {
        return counters;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
