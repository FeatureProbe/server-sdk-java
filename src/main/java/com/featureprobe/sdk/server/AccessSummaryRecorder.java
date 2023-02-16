package com.featureprobe.sdk.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessSummaryRecorder {

    Map<String, List<Counter>> counters;

    long startTime;

    long endTime;

    AccessSummaryRecorder() {
        counters = new HashMap<>();
    }

    private AccessSummaryRecorder(AccessSummaryRecorder accessSummaryRecorder) {
        counters = new HashMap<>(accessSummaryRecorder.counters);
        startTime = accessSummaryRecorder.startTime;
        endTime = System.currentTimeMillis();
    }

    static final class Counter {

        long count;

        final Object value;

        final Long version;

        final Integer index;

        public Counter(Object value, Long version, Integer index) {
            this.value = value;
            this.version = version;
            this.index = index;
            count = 1L;
        }

        public void increment() {
            ++count;
        }

        public boolean isGroup(Long version, Integer index) {
            return  this.version.equals(version) && this.index.equals(index);
        }

        public long getCount() {
            return count;
        }

        public Object getValue() {
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
                if (counter.isGroup(accessEvent.getVersion(), accessEvent.getVariationIndex())) {
                    counter.increment();
                    return;
                }
            }
            counters.add(new Counter(accessEvent.getValue(), accessEvent.getVersion(), accessEvent.getVariationIndex()));
        } else {
            List<Counter> groups = new ArrayList<>(1);
            groups.add(new Counter(accessEvent.getValue(), accessEvent.getVersion(), accessEvent.getVariationIndex()));
            counters.put(accessEvent.getKey(), groups);
        }
    }

    public void clear() {
        counters = new HashMap<>();
    }

    public AccessSummaryRecorder snapshot() {
        return new AccessSummaryRecorder(this);
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
