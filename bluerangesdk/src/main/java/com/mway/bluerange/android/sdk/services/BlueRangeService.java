//
//  BlueRangeService.java
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

package com.mway.bluerange.android.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Base class for all Android services that need to be started in foreground
 * or in background mode.
 */
public abstract class BlueRangeService extends Service {

    private static final String BACKGROUND_MODE_FIELD = "backgroundModeOn";
    private Context context;
    private Thread thread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startAsStickyService(Context context) {
        start(context, true);
    }

    public void startAsNonStickyService(Context context) {
        start(context, false);
    }

    public void startAsThread(final Context context) {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BlueRangeService.this.context = context;
                onStarted();
            }
        });
        this.thread.setName("RelutionIoTService");
        this.thread.start();
    }

    private void start(Context context, boolean backgroundModeOn) {
        Intent intent = new Intent(context, this.getClass());
        intent.putExtra(BACKGROUND_MODE_FIELD, backgroundModeOn);
        context.startService(intent);
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        } else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        int cmd = super.onStartCommand(intent, flags, startId);

        // If service is restarted, the intent may be null.
        // See http://stackoverflow.com/questions/30465082/
        // passing-extras-from-activity-to-intent-throws-nullpointerexception
        if (intent == null) return cmd;

        // Set context to this
        this.context = this;

        // Call template method
        onStarted();

        boolean backgroundModeOn = intent.getBooleanExtra(BACKGROUND_MODE_FIELD, false);
        int serviceMode;
        if (backgroundModeOn) {
            serviceMode = START_STICKY;
        } else {
            serviceMode = START_NOT_STICKY;
        }
        return serviceMode;
    }

    /**
     * A template method called whenever the service is started.
     */
    protected abstract void onStarted();

    public Context getContext() {
        return context;
    }
}
