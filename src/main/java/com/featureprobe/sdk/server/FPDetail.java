package com.featureprobe.sdk.server;

import java.util.Optional;

public class FPDetail<T> {

    private T value;

    private Optional<Integer> ruleIndex;

    private Optional<Long> version;

    private String reason;

    public FPDetail() {
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Optional<Integer> getRuleIndex() {
        return ruleIndex;
    }

    public void setRuleIndex(Optional<Integer> ruleIndex) {
        this.ruleIndex = ruleIndex;
    }

    public Optional<Long> getVersion() {
        return version;
    }

    public void setVersion(Optional<Long> version) {
        this.version = version;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "FPDetail{" + "value=" + value
                + ", ruleIndex=" + ruleIndex
                + ", version=" + version
                + ", reason='" + reason
                + '\'' + '}';
    }
}
