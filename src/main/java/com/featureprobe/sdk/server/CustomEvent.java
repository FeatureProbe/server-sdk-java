package com.featureprobe.sdk.server;

public class CustomEvent extends Event {

    private final String name;

    private final Double value;

    public CustomEvent(String name, String user, Double value) {
        super("custom", System.currentTimeMillis(), user);
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }
}
