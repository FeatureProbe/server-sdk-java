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
