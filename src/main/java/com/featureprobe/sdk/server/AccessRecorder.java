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
            count = count + 1;
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
