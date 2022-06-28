package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface NumberMatcher {

    /**
     * @throws NumberFormatException if any string in {@code objects} could not been parsed before the first match
     */
    boolean match(double target, List<String> objects);

}
