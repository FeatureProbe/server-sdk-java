package com.featureprobe.sdk.example;

import com.featureprobe.sdk.server.FPConfig;
import com.featureprobe.sdk.server.FPDetail;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class FeatureProbeDemo {

    private static final FPConfig config = FPConfig.builder()
            .remoteUri("http://localhost:4007")
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

    private static final FeatureProbe fpClient =
            new FeatureProbe("server-8ed48815ef044428826787e9a238b9c6a479f98c", config);

    public static void main(String[] args) {

        FPUser user = new FPUser("user_id");
        user.with("city", "New York");

        double discount = fpClient.numberValue("commodity_spike_activity", user, 0);
        System.out.println("user1 discount is :" + discount);
        FPDetail<Double> detail = fpClient.numberDetail("commodity_spike_activity", user, 0);
        System.out.println("detail:" + detail.getReason());


        FPUser user2 = new FPUser("user_id2");
        user2.with("city", "Paris");
        discount = fpClient.numberValue("commodity_spike_activity", user2, 0);
        System.out.println("user2 discount is :" + discount);
        FPDetail<Double> detail2 = fpClient.numberDetail("commodity_spike_activity", user2, 0);
        System.out.println("detail2:" + detail2.getReason());
        System.out.println("rule index:" + detail2.getRuleIndex().get());
    }

}
