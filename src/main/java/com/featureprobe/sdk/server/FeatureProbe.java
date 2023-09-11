/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.featureprobe.sdk.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.sdk.server.model.Segment;
import com.featureprobe.sdk.server.model.Toggle;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A client for the FeatureProbe API. Client instances are thread-safe.
 * Applications should instantiate a single {@code FeatureProbe} for the lifetime of their application.
 */
public final class FeatureProbe {

    private static final Logger logger = Loggers.MAIN;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String REASON_TYPE_MISMATCH = "Toggle data type mismatch";
    private static final String REASON_HANDLE_ERROR = "FeatureProbe handle error";

    private static final String LOG_HANDLE_ERROR = "FeatureProbe handle error. toggleKey: {}";
    private static final String LOG_CONVERSION_ERROR = "Toggle data type conversion error. toggleKey: {}";

    @VisibleForTesting
    final DataRepository dataRepository;

    @VisibleForTesting
    Synchronizer synchronizer;

    @VisibleForTesting
    EventProcessor eventProcessor;

    @VisibleForTesting
    FPConfig config;

    @VisibleForTesting
    private FeatureProbe(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        FPConfig config = FPConfig.DEFAULT;
        this.config = config;
        final FPContext context = new FPContext("test", config);
        eventProcessor = config.eventProcessorFactory.createEventProcessor(context);
    }

    /**
     * Creates a new client instance that connects to FeatureProbe with the default configuration.
     *
     * @param serverSDKKey for your FeatureProbe environment
     */
    public FeatureProbe(String serverSDKKey) {
        this(serverSDKKey, FPConfig.DEFAULT);
    }

    /**
     * Creates a new client to connect to FeatureProbe with a custom configuration.
     *
     * @param serverSDKKey for your FeatureProbe environment
     * @param config       the configuration control FeatureProbe client behavior
     */
    public FeatureProbe(String serverSDKKey, FPConfig config) {
        if (StringUtils.isBlank(serverSDKKey)) {
            throw new IllegalArgumentException("serverSDKKey must not be blank");
        }
        final FPContext context = new FPContext(serverSDKKey, config);
        this.config = config;
        this.eventProcessor = config.eventProcessorFactory.createEventProcessor(context);
        this.dataRepository = config.dataRepositoryFactory.createDataRepository(context);
        this.synchronizer = config.synchronizerFactory.createSynchronizer(context, dataRepository);
        Future<Void> startFuture = this.synchronizer.sync();
        try {
            startFuture.get(config.startWait, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            logger.error("Timeout encountered waiting for FeatureProbe client initialization");
        } catch (Exception e) {
            logger.error("Exception encountered waiting for FeatureProbe client initialization", e);
        }
        if (!this.dataRepository.initialized()) {
            logger.warn("FeatureProbe client was not successfully initialized");
        }
    }

    /**
     * Get the evaluated value of a boolean toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @return
     */
    public boolean boolValue(String toggleKey, FPUser user, boolean defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, Boolean.class);
    }

    /**
     * Get the evaluated value of a string toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @return
     */
    public String stringValue(String toggleKey, FPUser user, String defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, String.class);
    }

    /**
     * Get the evaluated value of a number toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @return
     */
    public double numberValue(String toggleKey, FPUser user, double defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, Double.class);
    }

    /**
     * Get the evaluated value of a json toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T jsonValue(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        return jsonEvaluate(toggleKey, user, defaultValue, clazz);
    }

    /**
     * Get detailed evaluation results of boolean toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @return
     */
    public FPDetail<Boolean> boolDetail(String toggleKey, FPUser user, boolean defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, Boolean.class);
    }

    /**
     * Get detailed evaluation results of string toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @return
     */
    public FPDetail<String> stringDetail(String toggleKey, FPUser user, String defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, String.class);
    }

    /**
     * Get detailed evaluation results of number toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @return
     */
    public FPDetail<Double> numberDetail(String toggleKey, FPUser user, double defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, Double.class);
    }

    /**
     * Get detailed evaluation results of json toggle
     *
     * @param toggleKey
     * @param user         {@link FPUser}
     * @param defaultValue
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> FPDetail<T> jsonDetail(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        return jsonEvaluateDetail(toggleKey, user, defaultValue, clazz);
    }

    /**
     * Manually events push
     */
    public void flush() {
        eventProcessor.flush();
    }

    /**
     * Safely shut down FeatureProbe instance
     *
     * @throws IOException
     */
    public void close() throws IOException {
        logger.info("Closing FeatureProbe Client。");
        eventProcessor.shutdown();
        synchronizer.close();
        dataRepository.close();
    }

    /**
     * Get FeatureProbe instance initial state
     *
     * @return
     */
    public boolean initialized() {
        return dataRepository.initialized();
    }

    /**
     * Tracks that a custom defined event
     *
     * @param eventName the name of the event
     * @param user      {@link FPUser}
     */
    public void track(String eventName, FPUser user) {
        eventProcessor.push(new CustomEvent(eventName, user, null));
    }

    /**
     * Tracks that a custom defined event, and provides an additional numeric value for custom event.
     *
     * @param eventName the name of the event
     * @param user      {@link FPUser}
     * @param value     a numeric value
     */
    public void track(String eventName, FPUser user, double value) {
        eventProcessor.push(new CustomEvent(eventName, user, value));
    }

    private <T> T jsonEvaluate(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        try {
            Toggle toggle = dataRepository.getToggle(toggleKey);
            Map<String, Segment> segments = dataRepository.getAllSegment();
            Map<String, Toggle> toggles = dataRepository.getAllToggle();
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, toggles, segments, defaultValue,
                        config.prerequisiteDeep);
                String value = mapper.writeValueAsString(evalResult.getValue());
                trackEvent(toggle, evalResult, user);
                return mapper.readValue(value, clazz);
            }
        } catch (JsonProcessingException e) {
            logger.error(LOG_CONVERSION_ERROR, toggleKey, e);
        } catch (Exception e) {
            logger.error(LOG_HANDLE_ERROR, toggleKey, e);
        }
        return defaultValue;
    }

    private <T> T genericEvaluate(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        try {
            Toggle toggle = dataRepository.getToggle(toggleKey);
            Map<String, Segment> segments = dataRepository.getAllSegment();
            Map<String, Toggle> toggles = dataRepository.getAllToggle();
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, toggles, segments, defaultValue,
                        config.prerequisiteDeep);
                trackEvent(toggle, evalResult, user);
                return clazz.cast(evalResult.getValue());
            }
        } catch (ClassCastException e) {
            logger.error(LOG_CONVERSION_ERROR, toggleKey, e);
        } catch (Exception e) {
            logger.error(LOG_HANDLE_ERROR, toggleKey, e);
        }
        return defaultValue;
    }

    private <T> FPDetail<T> jsonEvaluateDetail(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        FPDetail<T> detail = new FPDetail<>();
        try {
            return getEvaluateDetail(toggleKey, user, defaultValue, clazz, true);
        } catch (ClassCastException | JsonProcessingException e) {
            logger.error(LOG_CONVERSION_ERROR, toggleKey, e);
            detail.setReason(REASON_TYPE_MISMATCH);
        } catch (Exception e) {
            logger.error(LOG_HANDLE_ERROR, toggleKey, e);
            detail.setReason(REASON_HANDLE_ERROR);
        }
        detail.setValue(defaultValue);
        return detail;
    }

    private <T> FPDetail<T> genericEvaluateDetail(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        FPDetail<T> detail = new FPDetail<>();
        try {
            return getEvaluateDetail(toggleKey, user, defaultValue, clazz, false);
        } catch (ClassCastException | JsonProcessingException e) {
            logger.error(LOG_CONVERSION_ERROR, toggleKey, e);
            detail.setReason(REASON_TYPE_MISMATCH);
        } catch (Exception e) {
            logger.error(LOG_HANDLE_ERROR, toggleKey, e);
            detail.setReason(REASON_HANDLE_ERROR);
        }
        detail.setValue(defaultValue);
        return detail;
    }

    private <T> FPDetail<T> getEvaluateDetail(String toggleKey, FPUser user, T defaultValue,
                                              Class<T> clazz,
                                              boolean isJson) throws ClassCastException, JsonProcessingException {
        FPDetail<T> detail = new FPDetail<>();
        if (this.dataRepository.initialized()) {
            Toggle toggle = dataRepository.getToggle(toggleKey);
            Map<String, Segment> segments = dataRepository.getAllSegment();
            Map<String, Toggle> toggles = dataRepository.getAllToggle();
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, toggles, segments, defaultValue,
                        config.prerequisiteDeep);
                if (isJson) {
                    String res = mapper.writeValueAsString(evalResult.getValue());
                    detail.setValue(mapper.readValue(res, clazz));
                } else {
                    detail.setValue(clazz.cast(evalResult.getValue()));
                }
                detail.setReason(evalResult.getReason());
                detail.setRuleIndex(evalResult.getRuleIndex());
                detail.setVersion(Optional.of(evalResult.getVersion()));
                trackEvent(toggle, evalResult, user);
            } else {
                detail.setReason("Toggle not exist");
                detail.setValue(defaultValue);
            }
        } else {
            detail.setReason("FeatureProbe repository uninitialized");
            detail.setValue(defaultValue);
        }
        return detail;
    }

    private void trackEvent(Toggle toggle, EvaluationResult evalResult, FPUser user) {
        eventProcessor.push(buildAccessEvent(toggle, evalResult, user));
        if (Objects.nonNull(dataRepository.getDebugUntilTime())
                && dataRepository.getDebugUntilTime() >= System.currentTimeMillis()) {
            eventProcessor.push(buildDebugEvent(toggle, evalResult, user));
        }
    }

    private Event buildAccessEvent(Toggle toggle, EvaluationResult evalResult, FPUser user) {
        boolean trackAccessEvents =
                Objects.isNull(toggle.getTrackAccessEvents()) ? false : toggle.getTrackAccessEvents().booleanValue();
        return new AccessEvent(user, toggle.getKey(), evalResult.getValue(),
                evalResult.getVersion(), evalResult.getVariationIndex().orElse(null),
                evalResult.getRuleIndex().orElse(null), trackAccessEvents);
    }

    private Event buildDebugEvent(Toggle toggle, EvaluationResult evalResult, FPUser user) {
        return new DebugEvent(user, toggle.getKey(), evalResult.getValue(),
                evalResult.getVersion(), evalResult.getVariationIndex().orElse(null),
                evalResult.getRuleIndex().orElse(null), evalResult.getReason());
    }

}
