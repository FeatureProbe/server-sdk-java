package com.featureprobe.sdk.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class Loggers {

    private Loggers() {
    }

    static final String BASE_LOGGER_NAME = FeatureProbe.class.getName();
    static final String SYNCHRONIZER_LOGGER_NAME = BASE_LOGGER_NAME + "-Synchronizer";
    static final String EVENT_LOGGER_NAME = BASE_LOGGER_NAME + "-Event";

    static final Logger MAIN = LoggerFactory.getLogger(BASE_LOGGER_NAME);
    static final Logger SYNCHRONIZER = LoggerFactory.getLogger(SYNCHRONIZER_LOGGER_NAME);
    static final Logger EVENT = LoggerFactory.getLogger(EVENT_LOGGER_NAME);

}
