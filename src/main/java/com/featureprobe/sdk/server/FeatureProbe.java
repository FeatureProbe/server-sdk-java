package com.featureprobe.sdk.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.sdk.server.model.Toggle;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;

public final class FeatureProbe {

    private static final Logger logger = Loggers.MAIN;

    private ObjectMapper mapper = new ObjectMapper();

    @VisibleForTesting
    final DataRepository dataRepository;

    @VisibleForTesting
    EventProcessor eventProcessor;

    @VisibleForTesting
    private FeatureProbe(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        FPConfig config = FPConfig.DEFAULT;
        final FPContext context = new FPContext("test", config);
        eventProcessor = config.eventProcessorFactory.createEventProcessor(context);
    }

    public FeatureProbe(String sdkKey) {
        this(sdkKey, FPConfig.DEFAULT);
    }

    public FeatureProbe(String sdkKey, FPConfig config) {
        if (StringUtils.isBlank(sdkKey)) {
            throw new NullPointerException("sdkKey must not be blank");
        }
        final FPContext context = new FPContext(sdkKey, config);
        eventProcessor = config.eventProcessorFactory.createEventProcessor(context);
        this.dataRepository = config.dataRepositoryFactory.createDataRepository(context);
        config.synchronizerFactory.createSynchronizer(context, dataRepository).sync();
    }

    public boolean boolValue(String toggleKey, FPUser user, boolean defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, Boolean.class).booleanValue();
    }

    public String stringValue(String toggleKey, FPUser user, String defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, String.class);
    }

    public double numberValue(String toggleKey, FPUser user, double defaultValue) {
        return genericEvaluate(toggleKey, user, defaultValue, Double.class).doubleValue();
    }

    public <T> T jsonValue(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        return jsonEvaluate(toggleKey, user, defaultValue, clazz);
    }

    public FPDetail<Boolean> boolDetail(String toggleKey, FPUser user, boolean defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, Boolean.class);
    }

    public FPDetail<String> stringDetail(String toggleKey, FPUser user, String defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, String.class);
    }

    public FPDetail<Double> numberDetail(String toggleKey, FPUser user, double defaultValue) {
        return genericEvaluateDetail(toggleKey, user, defaultValue, Double.class);
    }

    public <T> FPDetail<T> jsonDetail(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        return jsonEvaluateDetail(toggleKey, user, defaultValue, clazz);
    }

    public void flush() {
        eventProcessor.flush();
    }

    private <T> T jsonEvaluate(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        try {
            Toggle toggle = dataRepository.getToggle(toggleKey);
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, defaultValue);
                String value = mapper.writeValueAsString(evalResult.getValue());
                AccessEvent accessEvent = new AccessEvent(System.currentTimeMillis(), user,
                        toggleKey, String.valueOf(evalResult.getValue()), evalResult.getVersion(),
                        evalResult.getVariationIndex().get());
                eventProcessor.push(accessEvent);
                return mapper.readValue(value, clazz);
            }
        } catch (JsonProcessingException e) {
            logger.error("Toggle data type conversion error。toggleKey: {}", toggleKey, e);
        } catch (Exception e) {
            logger.error("FeatureProbe handle error. toggleKey: {}", toggleKey, e);
        }
        return defaultValue;
    }

    private <T> T genericEvaluate(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        try {

            Toggle toggle = dataRepository.getToggle(toggleKey);
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, defaultValue);
                AccessEvent accessEvent = new AccessEvent(System.currentTimeMillis(), user,
                        toggleKey, String.valueOf(evalResult.getValue()), evalResult.getVersion(),
                        evalResult.getVariationIndex().get());
                eventProcessor.push(accessEvent);
                return clazz.cast(evalResult.getValue());
            }
        } catch (ClassCastException e) {
            logger.error("Toggle data type conversion error。toggleKey: {}", toggleKey, e);
        } catch (Exception e) {
            logger.error("FeatureProbe handle error. toggleKey: {}", toggleKey, e);
        }
        return defaultValue;
    }

    private <T> FPDetail<T> jsonEvaluateDetail(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        FPDetail<T> detail = new FPDetail<>();
        try {
            return getEvaluateDetail(toggleKey, user, defaultValue, clazz, true);
        } catch (ClassCastException | JsonProcessingException e) {
            logger.error("Toggle data type conversion error. toggleKey: {}", toggleKey, e);
            detail.setReason("Toggle data type mismatch");
        } catch (Exception e) {
            logger.error("FeatureProbe handle error. toggleKey: {}", toggleKey, e);
            detail.setReason("FeatureProbe handle error");
        }
        detail.setValue(defaultValue);
        return detail;
    }

    private <T> FPDetail<T> genericEvaluateDetail(String toggleKey, FPUser user, T defaultValue, Class<T> clazz) {
        FPDetail<T> detail = new FPDetail<>();
        try {
            return getEvaluateDetail(toggleKey, user, defaultValue, clazz, false);
        } catch (ClassCastException | JsonProcessingException e) {
            logger.error("Toggle data type conversion error. toggleKey: {}", toggleKey, e);
            detail.setReason("Toggle data type mismatch");
        } catch (Exception e) {
            logger.error("FeatureProbe handle error. toggleKey: {}", toggleKey, e);
            detail.setReason("FeatureProbe handle error");
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
            if (Objects.nonNull(toggle)) {
                EvaluationResult evalResult = toggle.eval(user, defaultValue);
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
