package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public final class Rule {

    private Serve serve;

    private List<Condition> conditions;

    public HitResult hit(FPUser user, String toggleKey) {
        for (Condition condition : conditions) {
            if (!user.containAttr(condition.getSubject())) {
                return new HitResult(false,
                        Optional.of(String.format("Warning: User with key '%s' does not have attribute name '%s'",
                                user.getKey(), condition.getSubject())));
            }
            if (!condition.matchObjects(user)) {
                return new HitResult(false);
            }
        }
        return serve.evalIndex(user, toggleKey);
    }

    public Rule() {
    }

    public Rule(Serve serve, List<Condition> conditions) {
        this.serve = serve;
        this.conditions = conditions;
    }

    public Serve getServe() {
        return serve;
    }

    public void setServe(Serve serve) {
        this.serve = serve;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

}
