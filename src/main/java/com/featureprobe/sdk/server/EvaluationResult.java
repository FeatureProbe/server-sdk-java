package com.featureprobe.sdk.server;

import java.util.Optional;

public class EvaluationResult {

    private Object value;

    private Optional<Integer> ruleIndex;

    private Optional<Integer> variationIndex;

    private Long version;

    private String reason;

    public EvaluationResult(Object value, Optional<Integer> ruleIndex,
                            Optional<Integer> variationIndex, Long version, String reason) {
        this.value = value;
        this.ruleIndex = ruleIndex;
        this.variationIndex = variationIndex;
        this.version = version;
        this.reason = reason;
    }

    public Object getValue() {
        return value;
    }

    public Optional<Integer> getRuleIndex() {
        return ruleIndex;
    }

    public Optional<Integer> getVariationIndex() {
        return variationIndex;
    }

    public void setVariationIndex(Optional<Integer> variationIndex) {
        this.variationIndex = variationIndex;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getReason() {
        return reason;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setRuleIndex(Optional<Integer> ruleIndex) {
        this.ruleIndex = ruleIndex;
    }


    public void setReason(String reason) {
        this.reason = reason;
    }
}
