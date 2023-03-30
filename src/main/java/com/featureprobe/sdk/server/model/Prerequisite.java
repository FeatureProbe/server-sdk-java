package com.featureprobe.sdk.server.model;

public class Prerequisite {

    private String key;
    private Object value;

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
