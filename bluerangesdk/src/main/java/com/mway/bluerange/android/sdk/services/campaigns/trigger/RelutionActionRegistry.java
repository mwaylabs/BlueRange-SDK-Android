//
//  RelutionActionRegistry.java
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

package com.mway.bluerange.android.sdk.services.campaigns.trigger;

import android.content.Context;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.core.triggering.BeaconAction;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionRegistry;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.content.RelutionContentActionBuilder;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.notification.RelutionNotificationBuilder;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag.RelutionTagActionBuilder;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.IBeaconMessageActionMapper;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.RelutionTagMessageActionMapper;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class RelutionActionRegistry implements BeaconActionRegistry {

    // Tracing
    private static final String LOG_TAG = "BeaconActionRegistry";
    private Context context;
    private ITracer tracer;

    // Mapping
    private IBeaconMessageActionMapper iBeaconMessageActionMapper;
    private RelutionTagMessageActionMapper relutionTagMessageActionMapper;
    // Building
    private RelutionActionBuilder actionBuilderChain;

    public RelutionActionRegistry(Context context, ITracer tracer, IBeaconMessageActionMapper iBeaconMessageActionMapper,
                                  RelutionTagMessageActionMapper relutionTagMessageActionMapper) {
        this.context = context;
        this.tracer = tracer;
        initMappers(iBeaconMessageActionMapper, relutionTagMessageActionMapper);
        initBuilders();
    }

    private void initMappers(IBeaconMessageActionMapper iBeaconActionMapper,
                             RelutionTagMessageActionMapper relutionTagMessageActionMapper) {
        this.iBeaconMessageActionMapper = iBeaconActionMapper;
        this.relutionTagMessageActionMapper = relutionTagMessageActionMapper;
    }

    private void initBuilders() {
        addActionBuilder(new RelutionContentActionBuilder());
        addActionBuilder(new RelutionNotificationBuilder(context));
        addActionBuilder(new RelutionTagActionBuilder());
    }

    @Override
    public boolean isAvailable(BeaconMessage message) throws UnsupportedMessageException {
        if (message instanceof IBeaconMessage) {
            return iBeaconMessageActionMapper.isAvailable();
        } else if (message instanceof RelutionTagMessageV1) {
            return relutionTagMessageActionMapper.isAvailable();
        }
        throw new UnsupportedMessageException();
    }

    @Override
    public List<BeaconAction> getBeaconActionsForMessage(BeaconMessage message)
            throws RegistryNotAvailableException, UnsupportedMessageException {
        // 1. Map message to action information
        RelutionActionInformation actionInformation = getActionInformationForMessage(message);
        // 2. Build actions from action information.
        List<BeaconAction> actions = createActionsFromActionInformation(actionInformation, message);
        if(actions.isEmpty()) {
            throw new UnsupportedMessageException();
        }
        return actions;
    }

    private RelutionActionInformation getActionInformationForMessage(BeaconMessage message)
            throws UnsupportedMessageException, RegistryNotAvailableException {
        RelutionActionInformation actionInformation;
        if (message instanceof IBeaconMessage) {
            IBeaconMessage iBeaconMessage = (IBeaconMessage)message;
            actionInformation = iBeaconMessageActionMapper.getBeaconActionInformation(iBeaconMessage);
        } else if (message instanceof RelutionTagMessageV1) {
            RelutionTagMessageV1 relutionTagMessage = (RelutionTagMessageV1)message;
            actionInformation = relutionTagMessageActionMapper.getBeaconActionInformation(relutionTagMessage);
        } else {
            throw new UnsupportedMessageException();
        }
        return actionInformation;
    }

    private List<BeaconAction> createActionsFromActionInformation(
            RelutionActionInformation actionInformation, BeaconMessage message) {
        List<BeaconAction> actions = new ArrayList<>();
        try {
            JSONObject jsonObject = actionInformation.getInformation();
            JSONArray campaignsArray = jsonObject.getJSONArray(RelutionCampaign.kCampaignsParameter);
            for (int i = 0; i < campaignsArray.length(); i++) {
                try {
                    // Create campaign
                    JSONObject campaignObject = campaignsArray.getJSONObject(i);
                    long begins = campaignObject.getLong(RelutionCampaign.kBeginsParameter);
                    Date beginsDate = new Date(begins);
                    long ends = campaignObject.getLong(RelutionCampaign.kEndsParameter);
                    Date endsDate = new Date(ends);
                    RelutionCampaign campaign = new RelutionCampaign(beginsDate, endsDate);

                    // Create actions
                    JSONArray actionsArray = campaignObject.getJSONArray(RelutionCampaign.kActionsParameter);
                    for (int j = 0; j < actionsArray.length(); j++) {
                        JSONObject actionObject = actionsArray.getJSONObject(j);
                        RelutionAction action = actionBuilderChain.createActionFromJSON(actionObject, message);
                        action.setCampaign(campaign);
                        action.setSourceBeaconMessage(message);
                        actions.add(action);
                    }
                } catch(JSONException e) {
                    // If a campaign does not have actions, just skip it.
                    tracer.logDebug(LOG_TAG, "Skipped action, because response is corrupt!");
                }
            }

        } catch (JSONException e) {
            tracer.logDebug(LOG_TAG, "Message could not be mapped to an action," +
                    " because response data from Relution are corrupt. " + e.getMessage());
        }
        return actions;
    }

    private void addActionBuilder(RelutionActionBuilder actionBuilder) {
        if (actionBuilderChain == null) {
            actionBuilderChain = actionBuilder;
        } else {
            actionBuilderChain.addChainElement(actionBuilder);
        }
    }
}
