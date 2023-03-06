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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.featureprobe.sdk.server.exceptions.HttpErrorException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultEventProcessor implements EventProcessor {

    private static final Logger logger = Loggers.EVENT;

    private static final int EVENT_BATCH_HANDLE_SIZE = 50;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    @VisibleForTesting
    private final BlockingQueue<EventAction> eventQueue;

    private final ScheduledExecutorService scheduler;

    private final int capacity = 10000;

    private final ExecutorService executor;

    private final Thread eventHandleThread;

    final EventRepository eventRepository = new EventRepository();

    final List<EventAction> actions = new ArrayList<>(EVENT_BATCH_HANDLE_SIZE);

    FPContext context;

    private static final String LOG_SENDER_ERROR = "Unexpected error from event sender";
    private static final String LOG_BUSY_EVENT = "Event processing is busy, some will be dropped";

    ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("FeatureProbe-event-handle-%d")
            .setPriority(1)
            .build();

    DefaultEventProcessor(FPContext context) {
        this.context = context;
        eventQueue = new ArrayBlockingQueue<>(capacity);
        executor = new ThreadPoolExecutor(1, 5, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), threadFactory);
        eventHandleThread = threadFactory.newThread(() -> {
            handleEvent(context, eventQueue, eventRepository);
        });
        eventHandleThread.setDaemon(true);
        eventHandleThread.start();
        Runnable flusher = this::flush;
        scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        scheduler.scheduleAtFixedRate(flusher, 0L, 5, TimeUnit.SECONDS);
    }

    @Override
    public void push(Event event) {
        if (!closed.get()) {
            boolean success = eventQueue.offer(new EventAction(EventActionType.EVENT, event));
            if (!success) {
                logger.warn(LOG_BUSY_EVENT);
            }
        }
    }

    @Override
    public void flush() {
        if (!closed.get()) {
            if (!eventQueue.offer(new EventAction(EventActionType.FLUSH, null))) {
                logger.warn(LOG_BUSY_EVENT);
            }
        }
    }

    @Override
    public void shutdown() {
        flush();
        doShutdown();
    }

    private void handleEvent(FPContext context, BlockingQueue<EventAction> eventQueue,
                             EventRepository eventRepository) {
        while (!closed.get() || !eventQueue.isEmpty()) {
            try {
                actions.clear();
                actions.add(eventQueue.take());
                eventQueue.drainTo(actions, EVENT_BATCH_HANDLE_SIZE - 1);
                for (EventAction action : actions) {
                    switch (action.type) {
                        case EVENT:
                            processEvent(action.event, eventRepository);
                            break;
                        case FLUSH:
                            processFlush(context, eventRepository);
                            break;
                        case SHUTDOWN:
                            doShutdown();
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                logger.error("FeatureProbe event handle error", e);
            }
        }
    }

    private void doShutdown() {
        if (closed.compareAndSet(false, true)) {
            try {
                eventHandleThread.join(2000);
                scheduler.awaitTermination(1000, TimeUnit.MILLISECONDS);
                executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("FeatureProbe shutdown error", e);
            }

        }
    }

    private void processEvent(Event event, EventRepository eventRepository) {
        eventRepository.add(event);
    }

    private void processFlush(FPContext context, EventRepository eventRepository) {
        if (eventRepository.isEmpty()) {
            return;
        }
        List<EventRepository> sendQueue = new ArrayList<>();
        sendQueue.add(eventRepository.snapshot());
        SendEventsTask task = new SendEventsTask(context, sendQueue);
        executor.submit(task);
        eventRepository.clear();
    }

    private static final class SendEventsTask implements Runnable {

        private final ObjectMapper mapper = new ObjectMapper();

        private final URL apiUrl;

        private final Headers headers;

        private final OkHttpClient httpClient;

        private final List<EventRepository> repositories;

        SendEventsTask(FPContext context, List<EventRepository> repositories) {
            this.apiUrl = context.getEventUrl();
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectionPool(context.getHttpConfiguration().connectionPool)
                    .connectTimeout(context.getHttpConfiguration().connectTimeout)
                    .readTimeout(context.getHttpConfiguration().readTimeout)
                    .writeTimeout(context.getHttpConfiguration().writeTimeout)
                    .retryOnConnectionFailure(false);
            httpClient = builder.build();
            headers = context.getHeaders();
            this.repositories = repositories;
        }

        @Override
        public void run() {
            Request request;
            try {
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                        mapper.writeValueAsString(repositories));
                request = new Request.Builder()
                        .url(apiUrl.toString())
                        .headers(headers)
                        .post(requestBody)
                        .build();
            } catch (Exception e) {
                logger.error(LOG_SENDER_ERROR, e);
                return;
            }
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new HttpErrorException("Http request error: " + response.code());
                }
                logger.debug("Http response: {}", response);
            } catch (Exception e) {
                logger.error(LOG_SENDER_ERROR, e);
            }
        }

    }

    private static final class EventRepository {

        List<Event> events = new ArrayList<>();

        AccessSummaryRecorder access = new AccessSummaryRecorder();

        public EventRepository() {
        }

        private EventRepository(EventRepository repository) {
            this.events = new ArrayList<>(repository.events);
            this.access = repository.access.snapshot();
        }

        boolean isEmpty() {
            return events.isEmpty() && access.counters.isEmpty();
        }

        void add(Event event) {
            if (event instanceof AccessEvent) {
                access.add(event);
//                if (((AccessEvent) event).isTrackAccessEvents()) {
                if (true) {
                    events.add(event);
                }
            } else if (event instanceof CustomEvent) {
                events.add(event);
            }
        }

        EventRepository snapshot() {
            return new EventRepository(this);
        }

        void clear() {
            events.clear();
            access.clear();
        }

        public List<Event> getEvents() {
            return events;
        }

        public AccessSummaryRecorder getAccess() {
            return access;
        }
    }

    private final static class EventAction {

        private final EventActionType type;

        private final Event event;

        public EventAction(EventActionType type, Event event) {
            this.type = type;
            this.event = event;
        }

    }

    private enum EventActionType {
        EVENT,
        FLUSH,
        SHUTDOWN
    }

}
