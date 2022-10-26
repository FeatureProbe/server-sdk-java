package com.featureprobe.sdk.example;

import com.featureprobe.sdk.server.FPConfig;
import com.featureprobe.sdk.server.FPDetail;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.featureprobe.sdk.server.Loggers;
import groovy.util.logging.Slf4j;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class FeatureProbeDemo {

    private static final org.slf4j.Logger logger = Loggers.MAIN;

    // FeatureProbe server URL for local docker
    private static final String FEATURE_PROBE_SERVER_URL = "http://localhost:4009/server"; // "https://featureprobe.io/server";


    // Server Side SDK Key for your project and environment
    public static final String FEATURE_PROBE_SERVER_SDK_KEY = "server-8ed48815ef044428826787e9a238b9c6a479f98c";

    public static void main(String[] args) throws IOException {

        Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
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
            .with("userId", "00001");        // "userId" is used in rules, should be filled in.

        // Get toggle result for this user.
        final String YOUR_TOGGLE_KEY = "campaign_allow_list";

        Boolean isOpen = fpClient.boolValue(YOUR_TOGGLE_KEY, user, false);
        System.out.println("feature for this user is :" + isOpen);

        // Demo of Detail function.
        FPDetail<Boolean> isOpenDetail = fpClient.boolDetail(YOUR_TOGGLE_KEY, user, false);
        System.out.println("detail:" + isOpenDetail.getReason());
        System.out.println("rule index:" + isOpenDetail.getRuleIndex());

        fpClient.close();

    }

}
