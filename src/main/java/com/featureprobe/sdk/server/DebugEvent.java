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

public class DebugEvent extends Event {

    private final String key;
    private final Object value;

    private final Long version;

    private final Integer variationIndex;

    private final Integer ruleIndex;

    private final String reason;

    private FPUser userDetail;

    public DebugEvent(FPUser user, String key, Object value, Long version, Integer variationIndex,
                      Integer ruleIndex, String reason) {
        super("debug", System.currentTimeMillis(), user.getKey());
        this.value = value;
        this.version = version;
        this.variationIndex = variationIndex;
        this.key = key;
        this.ruleIndex = ruleIndex;
        this.reason = reason;
        this.userDetail = user;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Long getVersion() {
        return version;
    }

    public Integer getVariationIndex() {
        return variationIndex;
    }

    public Integer getRuleIndex() {
        return ruleIndex;
    }

    public String getReason() {
        return reason;
    }

    public FPUser getUserDetail() {
        return userDetail;
    }
}
