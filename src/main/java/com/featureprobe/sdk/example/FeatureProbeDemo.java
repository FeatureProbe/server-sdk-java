package com.featureprobe.sdk.example;

import com.featureprobe.sdk.server.FPConfig;
import com.featureprobe.sdk.server.FPDetail;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class FeatureProbeDemo {

    // FeatureProbe server URL for local docker
    // private static final String FEATURE_PROBE_SERVER_URL = "http://localhost:4007";

    // FeatureProbe server URL for featureprobe.io
    private static final String FEATURE_PROBE_IO_SERVER_URL = "https://featureprobe.io/server";


    // Server Side SDK Key for your project and environment
    public static final String SERVER_SDK_KEY = "server-bdad3af5cd1cb14e52a1b04d5c6aa6790de1ccb5";

    // Toggle you want to use
    public static final String TOGGLE_KEY = "feature_toggle02";


    public static void main(String[] args) throws MalformedURLException {

        Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        final FPConfig config = FPConfig.builder()
            .remoteUri(FEATURE_PROBE_IO_SERVER_URL)
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

        // Init FeatureProbe, share this FeatureProbe instance in your project.
        final FeatureProbe fpClient = new FeatureProbe(SERVER_SDK_KEY, config);

        // Create one user.
        FPUser user = new FPUser("00001") // key is for percentage rollout, normally use userId as key
            .with("userId", "00001");

        // Get toggle result for this user.
        Boolean isOpen = fpClient.boolValue(TOGGLE_KEY, user, false);
        System.out.println("feature for this user is :" + isOpen);

        // Demo of Detail function.
        FPDetail<Boolean> isOpenDetail = fpClient.boolDetail(TOGGLE_KEY, user, false);
        System.out.println("detail:" + isOpenDetail.getReason());
        System.out.println("rule index:" + isOpenDetail.getRuleIndex());
    }

}
