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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.HitResult;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Split {

    private static final int BUCKET_SIZE = 10000;

    private static final int INVALID_INDEX = -1;

    private List<List<List<Integer>>> distribution;

    private String bucketBy;

    private String salt;

    public Split() {
    }

    public Split(List<List<List<Integer>>> distribution) {
        this.distribution = distribution;
    }

    public HitResult findIndex(FPUser user, String toggleKey) {
        String hashKey = user.getKey();
        if (StringUtils.isNotBlank(bucketBy)) {
            if (user.containAttr(bucketBy)) {
                hashKey = user.getAttr(bucketBy);
            } else {
                return new HitResult(false,
                        Optional.of(String.format("Warning: User with key '%s' does not have attribute name '%s'",
                                user.getKey(), bucketBy)));
            }
        }
        int groupIndex = getGroup(hash(hashKey, getHashSalt(toggleKey), BUCKET_SIZE));
        return new HitResult(true, Optional.of(groupIndex),
                Optional.of(String.format("selected %d percentage group", groupIndex)));
    }

    @VisibleForTesting
    private int getGroup(int hashValue) {
        for (int i = 0; i < distribution.size(); i++) {
            List<List<Integer>> groups = distribution.get(i);
            for (List<Integer> range : groups) {
                if (hashValue >= range.get(0) && hashValue < range.get(1)) {
                    return i;
                }
            }
        }
        return INVALID_INDEX;
    }

    private int hash(String hashKey, String hashSalt, int bucketSize) {
        String value = hashKey + hashSalt;
        byte[] hashValue;
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(value.getBytes(StandardCharsets.UTF_8));
            hashValue = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("couldn't clone MessageDigest object", e);
        }
        byte[] bytes = Arrays.copyOfRange(hashValue, hashValue.length - 4, hashValue.length);
        return new BigInteger(1, bytes).mod(BigInteger.valueOf(bucketSize)).intValue();
    }

    private String getHashSalt(String toggleKey) {
        if (StringUtils.isNotBlank(salt)) {
            return salt;
        }
        return toggleKey;
    }

    public List<List<List<Integer>>> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<List<List<Integer>>> distribution) {
        this.distribution = distribution;
    }

    public String getBucketBy() {
        return bucketBy;
    }

    public void setBucketBy(String bucketBy) {
        this.bucketBy = bucketBy;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

}
