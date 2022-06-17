package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;

import java.util.List;
import java.util.Map;

public class Segment {

    private String uniqueId;

    private Long version;

    private List<SegmentRule> rules;

    public boolean contains(FPUser user, Map<String, Segment> segments) {
        for (SegmentRule rule : rules) {
            HitResult hitResult = rule.hit(user, segments);
            if (hitResult.isHit()) {
                return true;
            }
        }
        return false;
    }

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
