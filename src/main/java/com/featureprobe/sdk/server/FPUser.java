package com.featureprobe.sdk.server;

import java.util.HashMap;
import java.util.Map;

public class FPUser {

    private String key;

    private Map<String, String> attrs = new HashMap<>();


    public FPUser with(String key, String value) {
        attrs.put(key, value);
        return this;
    }

    public boolean containAttr(String attr) {
        return attrs.containsKey(attr);
    }


    public FPUser(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public String getAttr(String key) {
        return attrs.get(key);
    }

}
