package com.featureprobe.sdk.server;

public class DefaultEventProcessorFactory implements EventProcessorFactory {

    @Override
    public EventProcessor createEventProcessor(FPContext context) {
        return new DefaultEventProcessor(context);
    }

}
