package com.featureprobe.sdk.server;

import java.util.List;

@FunctionalInterface
public interface DatetimeMatcher {

    boolean match(long target, List<String> objects);

}
