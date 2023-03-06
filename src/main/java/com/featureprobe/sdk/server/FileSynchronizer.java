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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.sdk.server.model.Repository;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

final class FileSynchronizer implements Synchronizer {

    private static final Logger logger = Loggers.SYNCHRONIZER;

    private final CompletableFuture<Void> initFuture;

    DataRepository dataRepository;

    static final String DEFAULT_LOCATION = "datasource/repo.json";

    final String location;

    final ObjectMapper mapper = new ObjectMapper();

    FileSynchronizer(DataRepository dataRepository, String location) {
        this.dataRepository = dataRepository;
        this.initFuture = new CompletableFuture<>();
        this.location = location == null ? DEFAULT_LOCATION : location;
    }

    @Override
    public Future<Void> sync() {
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
        return initFuture;
    }

    @Override
    public void close() throws IOException {
    }

}
