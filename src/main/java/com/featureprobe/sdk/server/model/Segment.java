package com.featureprobe.sdk.server.model;

import java.util.List;

public class Segment {

    private String uniqueId;

    private Long version;

    private List<SegmentRule> rules;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<SegmentRule> getRules() {
        return rules;
    }

    public void setRules(List<SegmentRule> rules) {
        this.rules = rules;
    }

}
