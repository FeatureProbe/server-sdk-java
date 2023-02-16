package com.featureprobe.sdk.server;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AccessEvent extends Event {

    private final String key;
    private final Object value;

    private final Long version;

    private final Integer variationIndex;

    private final Integer ruleIndex;

    private final String reason;

    @JsonIgnore
    private boolean trackAccessEvents;

    public AccessEvent(String user, String key, Object value, Long version, Integer variationIndex,
                       Integer ruleIndex, String reason, boolean trackAccessEvents) {
        super("access", System.currentTimeMillis(), user);
        this.value = value;
        this.version = version;
        this.variationIndex = variationIndex;
        this.key = key;
        this.ruleIndex = ruleIndex;
        this.reason = reason;
        this.trackAccessEvents = trackAccessEvents;
    }


    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Long getVersion() {
        return version;
    }

    public Integer getVariationIndex() {
        return variationIndex;
    }

    public Integer getRuleIndex() {
        return ruleIndex;
    }

    public String getReason() {
        return reason;
    }

    public boolean isTrackAccessEvents() {
        return trackAccessEvents;
    }
}
