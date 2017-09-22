//
//  RelutionNotificationAction.java
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

package com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.mway.bluerange.android.sdk.R;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionAction;

/**
 *
 */
public class RelutionNotificationAction extends RelutionAction {

    public static final String kTypeVariableNotification = "NOTIFICATION";
    public static final String kContentParameter = "content";
    public static final String kIconParameter = "icon";

    private String iconUrl;
    private String content;

    private Context context;
    private static final long kVibrationDurationInMs = 500;

    public RelutionNotificationAction(Context context, String actionId) {
        super(actionId);
        this.context = context;
    }

    @Override
    public void execute() {
        // Create a notification builder
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this.context)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("BlueRange SDK")
                        .setContentText(getContent());

        // Configure the notification
        Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(kVibrationDurationInMs);

        // Issue the notification
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        notificationManager.notify(mNotificationId, builder.build());

        // Turn screen on.
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
        wakeLock.setReferenceCounted(false);
        if ((wakeLock != null) && (wakeLock.isHeld() == false)) {
            wakeLock.acquire(5000);
        }
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int hashCode() {
        return iconUrl.hashCode() + content.hashCode();
    }
}
