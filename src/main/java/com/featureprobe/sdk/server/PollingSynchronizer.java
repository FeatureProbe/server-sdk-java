package com.featureprobe.sdk.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.sdk.server.exceptions.HttpErrorException;
import com.featureprobe.sdk.server.model.Repository;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class PollingSynchronizer implements Synchronizer {

    private static final Logger logger = Loggers.SYNCHRONIZER;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DataRepository dataRepository;
    private final Duration refreshInterval;
    private final URL apiUrl;
    private volatile ScheduledFuture<?> worker;

    @VisibleForTesting
    Socket socket;

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
        this.headers = context.getHeaders();
        this.httpClient = new OkHttpClient.Builder()
                .connectionPool(context.getHttpConfiguration().connectionPool)
                .connectTimeout(context.getHttpConfiguration().connectTimeout)
                .readTimeout(context.getHttpConfiguration().readTimeout)
                .writeTimeout(context.getHttpConfiguration().writeTimeout)
                .retryOnConnectionFailure(false)
                .build();
        this.socket = connectSocket(context);
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
            if (socket != null) {
                socket.close();
                socket = null;
            }
        }
    }

    private void poll() {
        Request request = new Request.Builder()
                .url(apiUrl.toString())
                .headers(headers)
                .get()
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                throw new HttpErrorException(String.format("Http request error: code: %d, body: %s",
                        response.code(), response.body().toString()));
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

    private Socket connectSocket(FPContext context) {
        IO.Options sioOptions = IO.Options.builder()
                .setTransports(new String[]{WebSocket.NAME})
                .setPath(context.getRealtimeUri().getPath())
                .build();
        Socket sio = IO.socket(context.getRealtimeUri(), sioOptions);

        sio.on("connect", objects -> {
            logger.info("connect socketio success");
            Map<String, String> credentials = new HashMap<>(1);
            credentials.put("key", context.getServerSdkKey());
            sio.emit("register", credentials);
        });

        sio.on("update", objects -> {
            logger.info("socketio recv update event");
            poll();
        });

        sio.on("disconnect", objects -> logger.info("socketio disconnected"));

        sio.on("connect_error", objects -> logger.error("socketio error: {}", objects));

        return sio.connect();
    }
}
