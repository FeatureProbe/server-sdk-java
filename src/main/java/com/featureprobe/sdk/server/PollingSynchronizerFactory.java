package com.featureprobe.sdk.server;

public final class PollingSynchronizerFactory implements SynchronizerFactory {

    @Override
    public Synchronizer createSynchronizer(FPContext context, DataRepository dataRepository) {
        return new PollingSynchronizer(context, dataRepository);
    }

}
