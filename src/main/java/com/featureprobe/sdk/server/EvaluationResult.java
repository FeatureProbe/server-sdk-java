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

    public void setValue(Object value) {
        this.value = value;
    }

    public Optional<Integer> getRuleIndex() {
        return ruleIndex;
    }

    public void setRuleIndex(Optional<Integer> ruleIndex) {
        this.ruleIndex = ruleIndex;
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

    public void setReason(String reason) {
        this.reason = reason;
    }

}
