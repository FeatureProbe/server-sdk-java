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
import com.featureprobe.sdk.server.exceptions.HttpErrorException;
import com.featureprobe.sdk.server.model.Repository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class PollingSynchronizer implements Synchronizer {

    private static final Logger logger = Loggers.SYNCHRONIZER;
    private static final String GET_SDK_KEY_HEADER = "Authorization";
    final ObjectMapper mapper = new ObjectMapper();
    DataRepository dataRepository;
    private final Duration refreshInterval;
    private final URL apiUrl;
    private volatile ScheduledFuture<?> worker;
    private final OkHttpClient httpClient;
    private final Headers headers;

    private final CompletableFuture<Void> initFuture;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("FeatureProbe-Synchronizer-%d")
                    .setPriority(Thread.MIN_PRIORITY)
                    .build());

    PollingSynchronizer(FPContext context, DataRepository dataRepository) {
        this.refreshInterval = context.getRefreshInterval();
        this.apiUrl = context.getSynchronizerUrl();
        this.dataRepository = dataRepository;
        this.initFuture = new CompletableFuture<>();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(context.getHttpConfiguration().connectionPool)
                .connectTimeout(context.getHttpConfiguration().connectTimeout)
                .readTimeout(context.getHttpConfiguration().readTimeout)
                .writeTimeout(context.getHttpConfiguration().writeTimeout)
                .retryOnConnectionFailure(false);
        headers = context.getHeaders();
        httpClient = builder.build();
    }

    @Override
    public Future<Void> sync() {
        logger.info("starting FeatureProbe polling repository with interval {} ms", refreshInterval.toMillis());
        synchronized (this) {
            if (worker == null) {
                worker = scheduler.scheduleAtFixedRate(this::poll, 0L, refreshInterval.toMillis(),
                        TimeUnit.MILLISECONDS);
            }
        }
        return initFuture;
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing FeatureProbe PollingSynchronizer");
        synchronized (this) {
            if (worker != null) {
                worker.cancel(true);
                worker = null;
            }
        }
    }

    public void poll() {
        Request request = new Request.Builder()
                .url(apiUrl.toString())
                .headers(headers)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                throw new HttpErrorException(String.format("Http request error: code: {}, body: {}:" + response.code(),
                        response.body()));
            }
            logger.debug("Http response: {}", response);
            logger.debug("Http response body: {}", body);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Repository repository = mapper.readValue(body, Repository.class);
            dataRepository.refresh(repository);
            this.initFuture.complete(null);
        } catch (Exception e) {
            logger.error("Unexpected error from polling processor", e);
        }
    }
}
