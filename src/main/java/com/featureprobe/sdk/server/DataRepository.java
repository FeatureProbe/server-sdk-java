package com.featureprobe.sdk.server;

import com.featureprobe.sdk.server.model.Repository;
import com.featureprobe.sdk.server.model.Segment;
import com.featureprobe.sdk.server.model.Toggle;

import java.io.Closeable;
import java.util.Map;

public interface DataRepository extends Closeable {

    void refresh(Repository repository);

    Toggle getToggle(String key);

    Map<String, Toggle> getAllToggle();

    Segment getSegment(String key);

    Map<String, Segment> getAllSegment();

    boolean initialized();

}
