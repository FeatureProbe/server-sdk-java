package com.featureprobe.sdk.server;

public class Event {

    private final String kind;

    private final long time;

    private final String user;

    public Event(String kind, long time, String user) {
        this.kind = kind;
        this.time = time;
        this.user = user;
    }

    public String getKind() {
        return kind;
    }

    public long getTime() {
        return time;
    }

    public String getUser() {
        return user;
    }
}
