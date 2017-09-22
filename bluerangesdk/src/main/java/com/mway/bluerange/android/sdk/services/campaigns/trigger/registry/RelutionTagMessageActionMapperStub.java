//
//  RelutionTagMessageActionMapperStub.java
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

package com.mway.bluerange.android.sdk.services.campaigns.trigger.registry;

import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionInformation;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionCampaign;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionRegistry;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag.RelutionTagAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class RelutionTagMessageActionMapperStub
        extends BeaconMessageActionMapperStub implements RelutionTagMessageActionMapper {

    private Map<Long, JSONObject> relutionActionMap = new HashMap<>();
    private boolean unexpectedUnavailable = false;

    public RelutionTagMessageActionMapperStub() {
        try {
            addTagActionWithBananasTag(1);
            addTagActionWithApplesTag(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RelutionActionInformation getBeaconActionInformation(RelutionTagMessageV1 relutionTagMessage)
            throws RelutionActionRegistry.UnsupportedMessageException, RelutionActionRegistry.RegistryNotAvailableException {

        // Simulation of "unexpected unavaialbility"
        if (unexpectedUnavailable) {
            throw new RelutionActionRegistry.RegistryNotAvailableException();
        }

        RelutionActionInformation actionInformation = new RelutionActionInformation();
        List<Long> tags = relutionTagMessage.getTags();
        JSONObject jsonObject = new JSONObject();
        for (long tag : tags) {
            JSONObject tagJsonObject = relutionActionMap.get(tag);
            // If at least one tag is not supported, we throw an unsupported message exception.
            if (tagJsonObject == null) {
                throw new RelutionActionRegistry.UnsupportedMessageException();
            }
            // If not, we assemble all json objects together.
            jsonObject = mergeTagJsonObject(tagJsonObject, jsonObject);
        }
        actionInformation.setInformation(jsonObject);
        return actionInformation;
    }

    private JSONObject mergeTagJsonObject(JSONObject tagJsonObject, JSONObject jsonObject) {
        if (jsonObject.length() == 0) {
            jsonObject = tagJsonObject;
        } else {
            try {
                JSONArray campaigns = jsonObject.getJSONArray(RelutionCampaign.kCampaignsParameter);
                JSONArray tagCampaigns = tagJsonObject.getJSONArray(RelutionCampaign.kCampaignsParameter);
                for (int i = 0; i < tagCampaigns.length() ;i++) {
                    JSONObject tagCampaign = tagCampaigns.getJSONObject(i);
                    // Just simply add the campaign to the list of all campaigns
                    // even if the campaign does already exist in the array.
                    campaigns.put(tagCampaign);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    private JSONObject addTagActionWithBananasTag(long tag) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject visitedActionObject = new JSONObject();
        visitedActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        visitedActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionTagAction.kTypeVariableVisited);
        visitedActionObject.put(RelutionTagAction.kContentParameter, "Bananas");
        actionsArray.put(visitedActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        relutionActionMap.put(tag, jsonObject);
        return jsonObject;
    }

    private JSONObject addTagActionWithApplesTag(long tag) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject visitedActionObject = new JSONObject();
        visitedActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        visitedActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionTagAction.kTypeVariableVisited);
        visitedActionObject.put(RelutionTagAction.kContentParameter, "Apples");
        actionsArray.put(visitedActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        relutionActionMap.put(tag, jsonObject);
        return jsonObject;
    }

    @Override
    public boolean isAvailable() {
        // Is always available
        return true;
    }

    public void setUnexpectedUnavailable(boolean unexpectedUnavailable) {
        this.unexpectedUnavailable = unexpectedUnavailable;
    }

    public Map<Long, JSONObject> getTagActionMap() {
        return relutionActionMap;
    }
}
