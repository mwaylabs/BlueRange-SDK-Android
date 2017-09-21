//
//  Tracer.java
//  BlueRangeSDK
//
// Copyright (c) 2016-2017, M-Way Solutions GmbH
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the M-Way Solutions GmbH nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY M-Way Solutions GmbH ''AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL M-Way Solutions GmbH BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
