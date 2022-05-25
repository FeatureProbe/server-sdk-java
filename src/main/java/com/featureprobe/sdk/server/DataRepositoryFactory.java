package com.featureprobe.sdk.server;

public interface DataRepositoryFactory {

    DataRepository createDataRepository(FPContext context);

}
