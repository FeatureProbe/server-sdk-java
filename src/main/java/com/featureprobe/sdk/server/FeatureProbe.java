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

    Synchronizer synchronizer;

    @VisibleForTesting
    EventProcessor eventProcessor;

    @VisibleForTesting
    private FeatureProbe(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        FPConfig config = FPConfig.DEFAULT;
        final FPContext context = new FPContext("test", config);
        eventProcessor = config.eventProcessorFactory.createEventProcessor(context);
    }

    /**
     * Creates a new client instance that connects to FeatureProbe with the default configuration.
     * @param serverSDKKey for your FeatureProbe environment
     */
    public FeatureProbe(String serverSDKKey) {
        this(serverSDKKey, FPConfig.DEFAULT);
    }

    /**
     * Creates a new client to connect to FeatureProbe with a custom configuration.
     * @param serverSDKKey for your FeatureProbe environment
     * @param config the configuration control FeatureProbe client behavior
     */
    public FeatureProbe(String serverSDKKey, FPConfig config) {
        if (StringUtils.isBlank(serverSDKKey)) {
            throw new IllegalArgumentException("serverSDKKey must not be blank");
        }
        final FPContext context = new FPContext(serverSDKKey, config);
        this.eventProcessor = config.eventProcessorFactory.createEventProcessor(context);
        this.dataRepository = config.dataRepositoryFactory.createDataRepository(context);
        this.synchronizer = config.synchronizerFactory.createSynchronizer(context, dataRepository);
        this.synchronizer.sync();
    }

    /**
     * Get the evaluated value of a boolean toggle
     * @param toggleKey
     * @param user {@link FPUser}
     * @param defaultValue
     * @return
     */
    public boolean boolValue(String toggleKey, FPUser user, boolean defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, Boolean.class);
    }

    /**
     * Get the evaluated value of a string toggle
     * @param toggleKey
     * @param user {@link FPUser}
     * @param defaultValue
     * @return
     */
    public String stringValue(String toggleKey, FPUser user, String defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, String.class);
    }

    /**
     * Get the evaluated value of a number toggle
     * @param toggleKey
     * @param user {@link FPUser}
     * @param defaultValue
     * @return
     */
    public double numberValue(String toggleKey, FPUser user, double defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, Double.class);
    }

    /**
     * Get the evaluated value of a json toggle
     * @param toggleKey
     * @param user {@link FPUser}
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
     * @param toggleKey
     * @param user {@link FPUser}
     * @param defaultValue
     * @return
     */
    public FPDetail<Boolean> boolDetail(String toggleKey, FPUser user, boolean defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, Boolean.class);
    }

    /**
     * Get detailed evaluation results of string toggle
     * @param toggleKey
     * @param user {@link FPUser}
     * @param defaultValue
     * @return
     */
    public FPDetail<String> stringDetail(String toggleKey, FPUser user, String defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, String.class);
    }

    /**
     * Get detailed evaluation results of number toggle
     * @param toggleKey
     * @param user {@link FPUser}
     * @param defaultValue
     * @return
     */
    public FPDetail<Double> numberDetail(String toggleKey, FPUser user, double defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, Double.class);
    }

    /**
     * Get detailed evaluation results of json toggle
     * @param toggleKey
     * @param user {@link FPUser}
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
     * @throws IOException
     */
    public void close() throws IOException {
        eventProcessor.shutdown();
        synchronizer.close();
        dataRepository.close();
    }

    private <T> T jsonEvaluate(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        try {
            Toggle toggle = dataRepository.getToggle(toggleKey);
            Map<String, Segment> segments = dataRepository.getAllSegment();
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, segments, defaultValue);
                String value = mapper.writeValueAsString(evalResult.getValue());
                AccessEvent accessEvent = new AccessEvent(System.currentTimeMillis(), user,
                        toggleKey, String.valueOf(evalResult.getValue()), evalResult.getVersion(),
                        evalResult.getVariationIndex().get());
                eventProcessor.push(accessEvent);
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
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, segments, defaultValue);
                AccessEvent accessEvent = new AccessEvent(System.currentTimeMillis(), user,
                        toggleKey, String.valueOf(evalResult.getValue()), evalResult.getVersion(),
                        evalResult.getVariationIndex().get());
                eventProcessor.push(accessEvent);
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
                                              Class<T> clazz, boolean isJson)
            throws ClassCastException, JsonProcessingException {
        FPDetail<T> detail = new FPDetail<>();
        if (this.dataRepository.initialized()) {
            Toggle toggle = dataRepository.getToggle(toggleKey);
            Map<String, Segment> segments = dataRepository.getAllSegment();
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, segments, defaultValue);
                if (isJson) {
                    String res = mapper.writeValueAsString(evalResult.getValue());
                    detail.setValue(mapper.readValue(res, clazz));
                } else {
                    detail.setValue(clazz.cast(evalResult.getValue()));
                }
                detail.setReason(evalResult.getReason());
                detail.setRuleIndex(evalResult.getRuleIndex());
                detail.setVersion(Optional.of(evalResult.getVersion()));
                AccessEvent accessEvent = new AccessEvent(System.currentTimeMillis(), user,
                        toggleKey, String.valueOf(evalResult.getValue()), evalResult.getVersion(),
                        evalResult.getVariationIndex().get());
                eventProcessor.push(accessEvent);
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

}
