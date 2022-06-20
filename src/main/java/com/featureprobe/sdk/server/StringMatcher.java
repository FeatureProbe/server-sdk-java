package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface StringMatcher {

    boolean match(String target, List<String> objects);

}
