package com.featureprobe.sdk.server;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.List;

@FunctionalInterface
public interface SemverMatcher {

    boolean match(ComparableVersion customValue, List<String> objects);

}
