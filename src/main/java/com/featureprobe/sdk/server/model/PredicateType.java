package com.featureprobe.sdk.server.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PredicateType {
    IS_ONE_OF("is one of"),
    ENDS_WITH("ends with"),
    STARTS_WITH("starts with"),
    CONTAINS("contains"),
    MATCHES_REGEX("matches regex"),
    IS_NOT_ANY_OF("is not any of"),
    DOES_NOT_END_WITH("does not end with"),
    DOES_NOT_START_WITH("does not start with"),
    DOES_NOT_CONTAIN("does not contain"),
    DOES_NOT_MATCH_REGEX("does not match regex"),

    IS_IN("is in"),
    IS_NOT_IN("is not in");


    private final String value;

    private static final Map<String, PredicateType> namesMap = Arrays.stream(PredicateType.values())
            .collect(Collectors.toMap(pt -> pt.value, pt -> pt));

    PredicateType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PredicateType forValue(String value) {
        return namesMap.get(value);
    }

    @JsonValue
    public String toValue() {
        return value;
    }

}
