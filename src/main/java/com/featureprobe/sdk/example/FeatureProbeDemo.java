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

package com.featureprobe.sdk.example;

import com.featureprobe.sdk.server.FPConfig;
import com.featureprobe.sdk.server.FPDetail;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.featureprobe.sdk.server.Loggers;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class FeatureProbeDemo {

    private static final org.slf4j.Logger logger = Loggers.MAIN;

    // FeatureProbe server URL for local docker
    private static final String FEATURE_PROBE_SERVER_URL = "http://localhost:4009/server"; // "https://featureprobe.io/server";

    // Server Side SDK Key for your project and environment
    public static final String FEATURE_PROBE_SERVER_SDK_KEY = "server-8ed48815ef044428826787e9a238b9c6a479f98c";

    public static void main(String[] args) throws IOException, InterruptedException {

        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        final FPConfig config = FPConfig.builder()
                .remoteUri(FEATURE_PROBE_SERVER_URL)
                .startWait(5L, TimeUnit.SECONDS)
                .build();

        // Init FeatureProbe, share this FeatureProbe instance in your project.
        final FeatureProbe fpClient = new FeatureProbe(FEATURE_PROBE_SERVER_SDK_KEY, config);

        if (!fpClient.initialized()) {
            logger.error("SDK failed to initialize!");
        }

        // Create one user.
        FPUser user = new FPUser()
                .with("userId", "00001"); // "userId" is used in rules, should be filled in.

        // Get toggle result for this user.
        final String YOUR_TOGGLE_KEY = "campaign_allow_list";

        Boolean isOpen = fpClient.boolValue(YOUR_TOGGLE_KEY, user, false);
        System.out.println("feature for this user is :" + isOpen);

        // Demo of Detail function.
        FPDetail<Boolean> isOpenDetail = fpClient.boolDetail(YOUR_TOGGLE_KEY, user, false);
        System.out.println("detail:" + isOpenDetail.getReason());
        System.out.println("rule index:" + isOpenDetail.getRuleIndex());

        // Simulate conversion rate of 1000 users for a new feature
        final String YOUR_CUSTOM_EVENT_NAME = "new_feature_conversion";
        for (int i = 0; i < 1000; i++) {
            FPUser eventUser = new FPUser().stableRollout(String.valueOf(System.nanoTime()));
            boolean newFeature = fpClient.boolValue(YOUR_TOGGLE_KEY, eventUser, false);
            Random random = new Random();
            int randomRang = random.nextInt(100);
            if (newFeature) {
                if (randomRang <= 55) {
                    System.out.println("New feature conversion.");
                    fpClient.track(YOUR_CUSTOM_EVENT_NAME, eventUser);
                }
            } else {
                if (randomRang > 55) {
                    System.out.println("Old feature conversion.");
                    fpClient.track(YOUR_CUSTOM_EVENT_NAME, eventUser);
                }
            }
            Thread.sleep(200);
        }

        fpClient.close();

    }

}
