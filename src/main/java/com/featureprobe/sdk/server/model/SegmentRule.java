package com.featureprobe.sdk.server.model;

import java.util.List;

public class SegmentRule {

    private List<Condition> conditions;

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

}
