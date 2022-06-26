package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface NumberMatcher {

    boolean match(float customValue, List<String> objects);

}
