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
