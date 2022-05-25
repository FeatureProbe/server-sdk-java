package com.featureprobe.sdk.server;

public class AccessEvent extends Event {

    private final String key;

    private final String value;

    private final Long version;

    private final Integer index;

    public AccessEvent(long timestamp, FPUser user, String key, String value, Long version, Integer index) {
        super(timestamp, user);
        this.key = key;
        this.value = value;
        this.version = version;
        this.index = index;
    }

    public String getKey() {
        return key;
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
