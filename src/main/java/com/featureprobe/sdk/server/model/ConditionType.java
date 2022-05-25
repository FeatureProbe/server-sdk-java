package com.featureprobe.sdk.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ConditionType {
    STRING, SEGMENT, DATE;

    private static Map<String, ConditionType> namesMap = new HashMap<>();

    static {
        namesMap.put("string", STRING);
        namesMap.put("segment", SEGMENT);
        namesMap.put("date", DATE);
    }

    @JsonCreator
    public static ConditionType forValue(String value) {
        return namesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return namesMap.entrySet().stream().filter(e -> e.getValue() == this).findFirst()
                .map(Map.Entry::getKey).orElse(null);
    }
}
