package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface SemVerMatcher {

    boolean match(Version target, List<String> objects);

}
