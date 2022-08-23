package com.featureprobe.sdk.example;

import com.featureprobe.sdk.server.FPConfig;
import com.featureprobe.sdk.server.FPDetail;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;
import java.net.MalformedURLException;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class FeatureProbeDemo {

    // FeatureProbe server URL for local docker
    private static final String FEATURE_PROBE_SERVER_URL = "http://localhost:4009/server"; // "https://featureprobe.io/server";

    // Server Side SDK Key for your project and environment
    public static final String FEATURE_PROBE_SERVER_SDK_KEY = "server-8ed48815ef044428826787e9a238b9c6a479f98c";

    public static void main(String[] args) throws MalformedURLException, InterruptedException {

        Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        final FPConfig config = FPConfig.builder()
            .remoteUri(FEATURE_PROBE_SERVER_URL)
            .build();

        // Init FeatureProbe, share this FeatureProbe instance in your project.
        final FeatureProbe fpClient = new FeatureProbe(FEATURE_PROBE_SERVER_SDK_KEY, config);

        // Create one user.
        FPUser user = new FPUser("00001")   // key is for percentage rollout, normally use userId as key
            .with("userId", "00001");            // "userId" is used in rules, should be filled in.

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
