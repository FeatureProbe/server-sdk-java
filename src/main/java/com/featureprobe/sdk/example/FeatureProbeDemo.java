package com.featureprobe.sdk.example;

import com.featureprobe.sdk.server.FPConfig;
import com.featureprobe.sdk.server.FPDetail;
import com.featureprobe.sdk.server.FPUser;
import com.featureprobe.sdk.server.FeatureProbe;

import java.time.Duration;

public class FeatureProbeDemo {

    private static final FPConfig config = FPConfig.builder()
            .remoteUri("http://localhost:4007")
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

    private static final FeatureProbe fpClient =
            new FeatureProbe("server-8ed48815ef044428826787e9a238b9c6a479f98c", config);

    public static void main(String[] args) {

        FPUser user = new FPUser("user_id").with("city", "New York");

        double discount = fpClient.numberValue("promotion_activity", user, 0);
        System.out.println("user in New York discount is :" + discount);
        FPDetail<Double> detail = fpClient.numberDetail("promotion_activity", user, 0);
        System.out.println("detail:" + detail.getReason());
        System.out.println("rule index:" + detail.getRuleIndex());


        FPUser user2 = new FPUser("user_id2");
        user2.with("city", "Paris");
        discount = fpClient.numberValue("promotion_activity", user2, 0);
        System.out.println("user in Paris discount is :" + discount);
        FPDetail<Double> detail2 = fpClient.numberDetail("promotion_activity", user2, 0);
        System.out.println("detail2:" + detail2.getReason());
        System.out.println("rule index:" + detail2.getRuleIndex());
    }

}
