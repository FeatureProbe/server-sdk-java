package com.featureprobe.sdk.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.sdk.server.model.Repository;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class FileSynchronizer implements Synchronizer {

    private static final Logger logger = Loggers.SYNCHRONIZER;

    DataRepository dataRepository;

    static final String DEFAULT_LOCATION = "datasource/repo.json";

    final String location;

    final ObjectMapper mapper = new ObjectMapper();

    FileSynchronizer(DataRepository dataRepository, String location) {
        this.dataRepository = dataRepository;
        this.location = location == null ? DEFAULT_LOCATION : location;
    }

    @Override
    public void sync() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(location)) {
            String data;
            if (is == null) {
                logger.error("repository file resource not found in classpath: {}", location);
                data = "";
            } else {
                data = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
            }
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Repository repository = mapper.readValue(data, Repository.class);
            dataRepository.refresh(repository);
        } catch (IOException e) {
            logger.error("repository file resource not found in classpath: {}", location, e);
        }
    }

    @Override
    public void close() throws IOException {
        return;
    }

}
