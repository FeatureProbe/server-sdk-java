package com.featureprobe.sdk.server;

import java.util.HashMap;

public class FPUser {

    private String key;

    private HashMap<String, String> attrs = new HashMap<>();


    public void with(String key, String value) {
        attrs.put(key, value);
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

    public HashMap<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(HashMap<String, String> attrs) {
        this.attrs = attrs;
    }
}
