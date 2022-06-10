package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.EvaluationResult;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;

import java.util.List;
import java.util.Optional;

public final class Toggle {

    private String key;

    private Boolean enabled;

    private Long version;

    private Serve disabledServe;

    private Serve defaultServe;

    private List<Rule> rules;

    private List<Object> variations;

    private Boolean forClient;

    public EvaluationResult eval(FPUser user, Object defaultValue) {
        String warning = "";

        if (!enabled) {
            return createDisabledResult(user, this.key, defaultValue);
        }

        if (rules != null && rules.size() > 0) {
            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                HitResult hitResult = rule.hit(user, this.key);
                if (hitResult.isHit()) {
                    return hitValue(hitResult, defaultValue, Optional.of(i));
                }
                warning = hitResult.getReason().orElse("");
            }
        }

        return createDefaultResult(user, this.key, defaultValue, warning);
    }

    private EvaluationResult createDisabledResult(FPUser user, String toggleKey, Object defaultValue) {
        EvaluationResult disabledResult = hitValue(disabledServe.evalIndex(user, this.key),
                defaultValue, Optional.empty());
        disabledResult.setReason("Toggle disabled");
        return disabledResult;
    }

    private EvaluationResult createDefaultResult(FPUser user, String toggleKey, Object defaultValue, String warning) {
        EvaluationResult defaultResult = hitValue(defaultServe.evalIndex(user, toggleKey), defaultValue,
                Optional.empty());
        defaultResult.setReason("Default rule hit. " + warning);
        return defaultResult;
    }

    private EvaluationResult hitValue(HitResult hitResult, Object defaultValue, Optional<Integer> ruleIndex) {
        EvaluationResult res = new EvaluationResult(defaultValue, ruleIndex, hitResult.getIndex(),
                this.version, hitResult.getReason().orElse(""));
        if (hitResult.getIndex().isPresent()) {
            Object variation = variations.get(hitResult.getIndex().get());
            if (defaultValue instanceof Double && variation instanceof Integer) {
                res.setValue(Double.valueOf((Integer) variation));
            } else {
                res.setValue(variation);
            }
            ruleIndex.ifPresent(idx -> res.setReason("Rule " + idx + " hit"));
        }
        return res;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Serve getDisabledServe() {
        return disabledServe;
    }

    public void setDisabledServe(Serve disabledServe) {
        this.disabledServe = disabledServe;
    }

    public Serve getDefaultServe() {
        return defaultServe;
    }

    public void setDefaultServe(Serve defaultServe) {
        this.defaultServe = defaultServe;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Object> getVariations() {
        return variations;
    }

    public void setVariations(List<Object> variations) {
        this.variations = variations;
    }

    public Boolean getForClient() {
        return forClient;
    }

    public void setForClient(Boolean forClient) {
        this.forClient = forClient;
    }

}
