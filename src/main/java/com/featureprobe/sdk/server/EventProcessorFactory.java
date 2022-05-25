package com.featureprobe.sdk.server;

public interface EventProcessorFactory {

    EventProcessor createEventProcessor(FPContext context);

}
