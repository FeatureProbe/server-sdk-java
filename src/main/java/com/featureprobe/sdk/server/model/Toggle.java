/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.featureprobe.sdk.server.model;

import com.featureprobe.sdk.server.EvaluationResult;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;
import com.featureprobe.sdk.server.exceptions.PrerequisitesDeepOverflowException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Toggle {

    private String key;

    private Boolean enabled;

    private Boolean trackAccessEvents;

    private Long lastModified;

    private Long version;

    private Serve disabledServe;

    private Serve defaultServe;

    private List<Rule> rules;

    private List<Object> variations;

    private List<Prerequisite> prerequisites;

    private Boolean forClient;

    public EvaluationResult eval(FPUser user, Map<String, Toggle> toggles, Map<String, Segment> segments,
                                 Object defaultValue, int deep) {

        String warning = "";

        if (!enabled) {
            return createDisabledResult(user, this.key, defaultValue);
        }

        if (deep <= 0) {
            throw new PrerequisitesDeepOverflowException("prerequisite deep overflow");
        }

        if (!prerequisite(user, toggles, segments, deep)) {
            return createDefaultResult(user, key, defaultValue, warning);
        }

        if (rules != null && rules.size() > 0) {
            for (int i = 0; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                HitResult hitResult = rule.hit(user, segments, this.key);
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

    private boolean prerequisite(FPUser user, Map<String, Toggle> toggles, Map<String, Segment> segments, int deep) {
        if (Objects.isNull(prerequisites) || prerequisites.isEmpty()) {
            return true;
        }
        try {
            for (Prerequisite prerequisite : prerequisites) {
                Toggle toggle = toggles.get(prerequisite.getKey());
                if (Objects.isNull(toggle))
                    return false;
                EvaluationResult eval = toggle.eval(user, toggles, segments, null, deep - 1);
                if (Objects.isNull(eval.getValue()))
                    return false;
                if (!eval.getValue().equals(prerequisite.getValue()))
                    return false;
            }
        } catch (PrerequisitesDeepOverflowException e) {
            throw e;
        }
        return true;
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

    public void setTrackAccessEvents(Boolean trackAccessEvents) {
        this.trackAccessEvents = trackAccessEvents;
    }

    public Boolean getTrackAccessEvents() {
        return trackAccessEvents;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public List<Prerequisite> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<Prerequisite> prerequisites) {
        this.prerequisites = prerequisites;
    }
}
