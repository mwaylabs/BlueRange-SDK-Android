//
//  BeaconMessageActionMapperStub.java
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

import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionCampaign;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 */
public abstract class BeaconMessageActionMapperStub {

    protected void addDefaultCampaign(JSONObject jsonObject, JSONArray actionsArray) throws JSONException {
        JSONObject campaign = getDefaultCampaign(actionsArray);
        JSONArray campaignArray = new JSONArray();
        campaignArray.put(campaign);
        jsonObject.put(RelutionCampaign.kCampaignsParameter, campaignArray);
    }

    protected JSONObject getDefaultCampaign(JSONArray actionsArray) throws JSONException {
        JSONObject campaign = new JSONObject();
        campaign.put(RelutionCampaign.kBeginsParameter, RelutionCampaign.kBeginsDefaultValue);
        campaign.put(RelutionCampaign.kEndsParameter, RelutionCampaign.kEndsDefaultValue);
        campaign.put(RelutionCampaign.kActionsParameter, actionsArray);
        return campaign;
    }

    protected void addExpiredCampaign(JSONObject jsonObject, JSONArray actionsArray) throws JSONException {
        JSONObject campaign = new JSONObject();
        campaign.put(RelutionCampaign.kBeginsParameter, RelutionCampaign.kBeginsDefaultValue);
        // We want the campaign to be expired.
        campaign.put(RelutionCampaign.kEndsParameter, 0);
        campaign.put(RelutionCampaign.kActionsParameter, actionsArray);
        JSONArray campaignArray = new JSONArray();
        campaignArray.put(campaign);
        jsonObject.put(RelutionCampaign.kCampaignsParameter, campaignArray);
    }

    protected void addInactiveCampaign(JSONObject jsonObject, JSONArray actionsArray) throws JSONException {
        JSONObject campaign = new JSONObject();
        // We want the campaign to be inactive.
        campaign.put(RelutionCampaign.kBeginsParameter, RelutionCampaign.kEndsDefaultValue);
        campaign.put(RelutionCampaign.kEndsParameter, RelutionCampaign.kEndsDefaultValue);
        campaign.put(RelutionCampaign.kActionsParameter, actionsArray);
        JSONArray campaignArray = new JSONArray();
        campaignArray.put(campaign);
        jsonObject.put(RelutionCampaign.kCampaignsParameter, campaignArray);
    }
}
