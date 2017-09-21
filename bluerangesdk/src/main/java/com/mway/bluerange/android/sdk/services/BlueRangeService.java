//
//  BlueRangeService.java
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
