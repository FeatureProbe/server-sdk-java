package com.featureprobe.sdk.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Loggers {

    private Loggers() {
    }

    private static final String BASE_LOGGER_NAME = FeatureProbe.class.getName();
    private static final String SYNCHRONIZER_LOGGER_NAME = BASE_LOGGER_NAME + "-Synchronizer";
    private static final String EVENT_LOGGER_NAME = BASE_LOGGER_NAME + "-Event";
    private static final String EVALUATOR_LOGGER_NAME = BASE_LOGGER_NAME + "-Evaluator";

    public static final Logger MAIN = LoggerFactory.getLogger(BASE_LOGGER_NAME);
    public static final Logger SYNCHRONIZER = LoggerFactory.getLogger(SYNCHRONIZER_LOGGER_NAME);
    public static final Logger EVENT = LoggerFactory.getLogger(EVENT_LOGGER_NAME);
    public static final Logger EVALUATOR = LoggerFactory.getLogger(EVALUATOR_LOGGER_NAME);

}
