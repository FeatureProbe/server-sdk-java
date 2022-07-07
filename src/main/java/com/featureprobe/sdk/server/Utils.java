package com.featureprobe.sdk.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static String readSdkVersion() throws IOException {
        InputStream in = Utils.class.getClassLoader().getResourceAsStream("main.properties");
        Properties properties = new Properties();
        properties.load(in);
        return properties.getProperty("version");
    }

}
