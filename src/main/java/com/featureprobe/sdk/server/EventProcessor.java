package com.featureprobe.sdk.server;

public interface EventProcessor {

    void push(Event event);

    void flush();

    void shutdown();

}
