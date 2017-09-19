//
//  RelutionActionRegistry.java
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
