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

import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Rule {

    private Serve serve;

    private List<Condition> conditions;

    public HitResult hit(FPUser user, Map<String, Segment> segments, String toggleKey) {
        if (user == null || toggleKey == null || toggleKey.isEmpty()) {
            return new HitResult(false);
        }
        for (Condition condition : conditions) {
            if (condition.getType() != ConditionType.SEGMENT
                    && condition.getType() != ConditionType.DATETIME
                    && !user.containAttr(condition.getSubject())) {
                return new HitResult(false,
                        Optional.of(String.format("Warning: User with key '%s' does not have attribute name '%s'",
                                user.getKey(), condition.getSubject())));
            }
            if (!condition.matchObjects(user, segments)) {
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
