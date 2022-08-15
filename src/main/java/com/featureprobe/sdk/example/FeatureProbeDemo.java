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
    public static final String SERVER_SDK_KEY = "server-8ed48815ef044428826787e9a238b9c6a479f98c";

    // Toggle you want to use
    public static final String TOGGLE_KEY = "promotion_activity";

    public static void main(String[] args) throws MalformedURLException {
        Logger root = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);

        final FPConfig config = FPConfig.builder()
//            .remoteUri(FEATURE_PROBE_SERVER_URL)
            .remoteUri(FEATURE_PROBE_IO_SERVER_URL)
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

        // Init FeatureProbe, share this FeatureProbe instance in your project.
        final FeatureProbe fpClient = new FeatureProbe(SERVER_SDK_KEY, config);

        // Create one user.
        FPUser user = new FPUser("user_id").with("city", "New York");

        // Get toggle result for this user.
        double discount = fpClient.numberValue(TOGGLE_KEY, user, 0);
        System.out.println("user in New York discount is :" + discount);

        // Demo of Detail function.
        FPDetail<Double> detail = fpClient.numberDetail(TOGGLE_KEY, user, 0);
        System.out.println("detail:" + detail.getReason());
        System.out.println("rule index:" + detail.getRuleIndex());

        // Create another user.
        FPUser user2 = new FPUser("user_id2").with("city", "Paris");

        // Get toggle result for the second user.
        discount = fpClient.numberValue(TOGGLE_KEY, user2, 0);
        System.out.println("user in Paris discount is :" + discount);

        // Demo of Detail function.
        FPDetail<Double> detail2 = fpClient.numberDetail(TOGGLE_KEY, user2, 0);
        System.out.println("detail2:" + detail2.getReason());
        System.out.println("rule index:" + detail2.getRuleIndex());
    }

}
