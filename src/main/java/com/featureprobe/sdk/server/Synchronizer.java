package com.featureprobe.sdk.server;

import java.io.Closeable;
import java.io.IOException;

public interface Synchronizer extends Closeable {

    void sync();

    void close() throws IOException;

}
