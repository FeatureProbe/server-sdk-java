package com.featureprobe.sdk.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ConditionType {
    STRING("string"),
    SEGMENT("segment"),
    DATETIME("datetime"),
    NUMBER("number"),
    SEM_VER("semver");

    private final String value;

    ConditionType(String value) {
        this.value = value;
    }

    private static final Map<String, ConditionType> namesMap = Arrays.stream(ConditionType.values())
            .collect(Collectors.toMap(ct -> ct.value, ct -> ct));

    @JsonCreator
    public static ConditionType forValue(String value) {
        return namesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return value;
    }

}
