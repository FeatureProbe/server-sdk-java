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

import com.google.common.annotations.VisibleForTesting;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class StreamingSynchronizer implements Synchronizer {

    private static final Logger logger = Loggers.SYNCHRONIZER;

    private PollingSynchronizer pollingSynchronizer;

    @VisibleForTesting
    Socket socket;

    StreamingSynchronizer(FPContext context, DataRepository dataRepository) {
        pollingSynchronizer = new PollingSynchronizer(context, dataRepository);
        this.socket = connectSocket(context);
    }

    @Override
    public Future<Void> sync() {
        return pollingSynchronizer.sync();
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        }
    }

    private Socket connectSocket(FPContext context) {
        IO.Options sioOptions = IO.Options.builder()
                .setTransports(new String[]{WebSocket.NAME})
                .setPath(context.getRealtimeUri().getPath())
                .build();
        Socket sio = IO.socket(context.getRealtimeUri(), sioOptions);

        sio.on("connect", objects -> {
            logger.info("connect socket-io success");
            Map<String, String> credentials = new HashMap<>(1);
            credentials.put("key", context.getServerSdkKey());
            sio.emit("register", credentials);
        });

        sio.on("update", objects -> {
            logger.info("socket-io recv update event");
            pollingSynchronizer.poll();
        });

        sio.on("disconnect", objects -> logger.info("socket-io disconnected"));

        sio.on("connect_error", objects -> logger.error("socket-io error: {}", objects));

        return sio.connect();
    }

}
