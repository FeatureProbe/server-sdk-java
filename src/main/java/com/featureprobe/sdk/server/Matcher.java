package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface Matcher {

    boolean match(String target, List<String> objects);

}
