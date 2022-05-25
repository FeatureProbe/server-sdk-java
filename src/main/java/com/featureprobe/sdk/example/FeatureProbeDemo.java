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
            .remoteUri("http://127.0.0.1:4007")
            .pollingMode(Duration.ofSeconds(3))
            .useMemoryRepository()
            .build();

    private static final FeatureProbe fpClient =
            new FeatureProbe("server-61db54ecea79824cae3ac38d73f1961d698d0477", config);

    public static void main(String[] args) {

        FPUser user = new FPUser("user_unique");
        user.with("userId", "122121211212");
        user.with("tel", "12345678998");

        boolean boolValue = fpClient.boolValue("bool_toggle_key", user, false);
        System.out.println("FeatureProbe evaluation boolean type toggle result is :" + boolValue);
        FPDetail<Boolean> boolDetail = fpClient.boolDetail("bool_toggle_key", user, false);
        System.out.println("FeatureProbe evaluation boolean type toggle result detail is :" + boolDetail.toString());

        String stringValue = fpClient.stringValue("string_toggle_key", user, "default");
        System.out.println("FeatureProbe evaluation string type toggle result is :" + stringValue);
        FPDetail<String> stringDetail = fpClient.stringDetail("string_toggle_key", user, "default");
        System.out.println("FeatureProbe evaluation string type toggle result detail is :" + stringDetail.toString());


        double numberValue = fpClient.numberValue("number_toggle_key", user, 0);
        System.out.println("FeatureProbe evaluation number type toggle result is :" + numberValue);
        FPDetail<Double> numberDetail = fpClient.numberDetail("number_toggle_key", user, 0);
        System.out.println("FeatureProbe evaluation number type toggle result detail is :" + numberDetail.toString());


        Map<String, String> defaultJson = new HashMap<>();
        defaultJson.put("name", "FeatureProbe");
        Map<String, String> jsonValue = fpClient.jsonValue("json_toggle_key", user, defaultJson, Map.class);
        System.out.println("FeatureProbe evaluation json type toggle result is :" + jsonValue);
        FPDetail<Map> jsonDetail = fpClient.jsonDetail("json_toggle_key", user, defaultJson, Map.class);
        System.out.println("FeatureProbe evaluation json type toggle result detail is :" + jsonDetail.toString());

    }

}
