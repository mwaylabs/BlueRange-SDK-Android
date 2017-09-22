//
//  RelutionNotificationBuilder.java
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

import android.content.Context;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionBuilder;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionAction;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public class RelutionNotificationBuilder extends RelutionActionBuilder {

    private Context context;

    public RelutionNotificationBuilder(Context context) {
        this.context = context;
    }

    @Override
    protected RelutionAction createActionFromJSONIfPossible(JSONObject actionObject, BeaconMessage message) throws JSONException {
        String actionType = actionObject.getString(RelutionAction.TYPE_PARAMETER);
        if (actionType.equals(RelutionNotificationAction.kTypeVariableNotification)) {
            RelutionNotificationAction notificationAction = new RelutionNotificationAction(context, "");
            String content = actionObject.getString(RelutionNotificationAction.kContentParameter);
            notificationAction.setContent(content);
            if (actionObject.has(RelutionNotificationAction.kIconParameter)) {
                String iconUrl = actionObject.getString(RelutionNotificationAction.kIconParameter);
                notificationAction.setIconUrl(iconUrl);
            }
            return notificationAction;
        } else {
            return null;
        }
    }
}
