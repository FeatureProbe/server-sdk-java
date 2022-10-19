package com.featureprobe.sdk.server;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Future;

public interface Synchronizer extends Closeable {

    Future<Void> sync();

    void close() throws IOException;

}
