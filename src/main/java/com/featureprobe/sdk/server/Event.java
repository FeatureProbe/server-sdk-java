package com.featureprobe.sdk.server;

public class Event {

    private final long createdTime;

    private final FPUser user;

    public Event(long createdTime, FPUser user) {
        this.createdTime = createdTime;
        this.user = user;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public FPUser getUser() {
        return user;
    }
}
