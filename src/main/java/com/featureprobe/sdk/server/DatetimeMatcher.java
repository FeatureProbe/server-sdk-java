package com.featureprobe.sdk.server;

@FunctionalInterface
public interface DatetimeMatcher {

    boolean match(long target, long customValue);

}
