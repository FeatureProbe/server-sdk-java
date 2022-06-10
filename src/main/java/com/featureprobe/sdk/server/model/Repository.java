package com.featureprobe.sdk.server.model;

import java.util.Map;

public final class Repository {

    private Map<String, Toggle> toggles;

    private Map<String, Segment> segments;

    public Repository() {
    }

    public Repository(Map<String, Toggle> toggles, Map<String, Segment> segments) {
        this.toggles = toggles;
        this.segments = segments;
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

}
