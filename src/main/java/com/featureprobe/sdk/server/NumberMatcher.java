package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface NumberMatcher {

    boolean match(double target, List<String> objects);

}
