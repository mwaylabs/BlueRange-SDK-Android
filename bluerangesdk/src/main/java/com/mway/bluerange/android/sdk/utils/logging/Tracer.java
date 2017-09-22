//
//  Tracer.java
//  BlueRangeSDK
//
// Copyright (c) 2016-2017, M-Way Solutions GmbH
// All rights reserved.
//
// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
//

package com.mway.bluerange.android.sdk.utils.logging;

import android.util.Log;

import com.mway.bluerange.android.sdk.common.Constants;

/**
 *
 */
public class Tracer implements ITracer {

    private static Tracer instance = null;
    private static boolean enabled = true;

    private Tracer() {}

    public static Tracer getInstance() {
        if (instance == null) {
            instance = new Tracer();
        }
        return instance;
    }

    public static void i(String tag, String message) {
        Tracer logger = getInstance();
        logger.logInfo(tag, message);
    }

    public static void d(String tag, String message) {
        Tracer logger = getInstance();
        logger.logDebug(tag, message);
    }

    public static void e(String tag, String message) {
        Tracer logger = getInstance();
        logger.logError(tag, message);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        Tracer.enabled = enabled;
    }

    @Override
    public void logInfo(String tag, String message) {
        if (enabled) {
            Log.i(getCompleteLogTag(tag), message);
        }
    }

    @Override
    public void logDebug(String tag, String message) {
        if (enabled) {
            Log.d(getCompleteLogTag(tag), message);
        }
    }

    @Override
    public void logWarning(String tag, String message) {
        if (enabled) {
            Log.w(getCompleteLogTag(tag), message);
        }
    }

    @Override
    public void logError(String tag, String message) {
        if (enabled) {
            Log.e(getCompleteLogTag(tag), message);
        }
    }

    private String getCompleteLogTag(String logTag) {
        return Constants.TAG + ":" + logTag;
    }
}
