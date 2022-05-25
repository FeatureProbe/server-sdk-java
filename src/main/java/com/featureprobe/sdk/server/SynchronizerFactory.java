package com.featureprobe.sdk.server;

public interface SynchronizerFactory {

    Synchronizer createSynchronizer(FPContext context, DataRepository dataRepository);

}
