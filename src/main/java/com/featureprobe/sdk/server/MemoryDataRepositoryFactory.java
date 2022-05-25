package com.featureprobe.sdk.server;

final class MemoryDataRepositoryFactory implements DataRepositoryFactory {

    @Override
    public DataRepository createDataRepository(FPContext context) {
        return new MemoryDataRepository();
    }

}
