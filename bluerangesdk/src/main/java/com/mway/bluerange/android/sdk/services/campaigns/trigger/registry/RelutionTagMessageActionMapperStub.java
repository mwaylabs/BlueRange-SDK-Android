//
//  RelutionTagMessageActionMapperStub.java
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
