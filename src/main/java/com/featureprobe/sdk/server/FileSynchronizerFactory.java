package com.featureprobe.sdk.server;

public final class FileSynchronizerFactory implements SynchronizerFactory {

    @Override
    public Synchronizer createSynchronizer(FPContext context, DataRepository dataRepository) {
        return new FileSynchronizer(dataRepository, context.getLocation());
    }

}
