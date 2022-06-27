package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SegmentRule {

    private List<Condition> conditions;

    public SegmentRule() {
    }

    public SegmentRule(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public HitResult hit(FPUser user, Map<String, Segment> segments) {
        for (Condition condition : conditions) {
            if (condition.getType() != ConditionType.SEGMENT && !user.containAttr(condition.getSubject())) {
                return new HitResult(false,
                        Optional.of(String.format("Warning: User with key '%s' does not have attribute name '%s'",
                                user.getKey(), condition.getSubject())));
            }
            if (!condition.matchObjects(user, segments)) {
                return new HitResult(false);
            }
        }
        return new HitResult(true);
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

}
