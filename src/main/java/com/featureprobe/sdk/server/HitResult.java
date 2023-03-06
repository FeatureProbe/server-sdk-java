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

public final class HitResult {

    private boolean hit;

    private Optional<Integer> index = Optional.empty();

    private Optional<String> reason = Optional.empty();

    public HitResult(boolean result) {
        this.hit = result;
    }

    public HitResult(boolean hit, Optional<String> reason) {
        this.hit = hit;
        this.reason = reason;
    }

    public HitResult(boolean result, Optional<Integer> index, Optional<String> reason) {
        this.hit = result;
        this.index = index;
        this.reason = reason;
    }

    public boolean isHit() {
        return hit;
    }

    public Optional<Integer> getIndex() {
        return index;
    }

    public Optional<String> getReason() {
        return reason;
    }
}
