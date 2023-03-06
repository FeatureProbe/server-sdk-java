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
