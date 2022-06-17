package com.featureprobe.sdk.server;

import com.featureprobe.sdk.server.model.Segment;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface SegmentMatcher {

    boolean match(FPUser user, Map<String, Segment> segments, List<String> objects);

}
