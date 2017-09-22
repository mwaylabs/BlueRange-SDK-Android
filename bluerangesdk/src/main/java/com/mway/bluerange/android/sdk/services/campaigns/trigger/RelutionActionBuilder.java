//
//  RelutionActionBuilder.java
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

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 *
 */
public abstract class RelutionActionBuilder {

    protected RelutionActionBuilder successor;

    public void addChainElement(RelutionActionBuilder chainElement) {
        if (successor == null) {
            this.successor = chainElement;
        } else {
            this.successor.addChainElement(chainElement);
        }
    }

    public RelutionAction createActionFromJSON(JSONObject actionObject, BeaconMessage message) throws JSONException {
        RelutionAction action = createActionFromJSONIfPossible(actionObject, message);
        if (action == null) {
            if (this.successor != null) {
                action = this.successor.createActionFromJSON(actionObject, message);
            }
        }
        // Besides the action specific parameters, we want to add common parameters, as well.
        addCommonParameters(actionObject, action);
        return action;
    }

    private void addCommonParameters(JSONObject actionObject, RelutionAction action) {
        addActionIdParameter(actionObject, action);
        addValidityBeginsParameter(actionObject, action);
        addValidUntilParameter(actionObject, action);
        addRepeatEveryParameter(actionObject, action);
        addDistanceThresholdParameter(actionObject, action);
    }

    private void addActionIdParameter(JSONObject actionObject, RelutionAction action) {
        try {
            String actionId = actionObject.getString(RelutionAction.ACTION_ID_PARAMETER);
            action.setActionId(actionId);
        } catch (JSONException e) {
            // We just take the default values, whenever parameters do
            // not exist or may be corrupt.
        }
    }

    private void addValidityBeginsParameter(JSONObject actionObject, RelutionAction action) {
        try {
            long postponeInMs = actionObject.getLong(RelutionAction.POSTPONE_PARAMETER) * 1000;
            long nowInMs = new Date().getTime();
            Date validityBegins = new Date(nowInMs + postponeInMs);
            action.setValidityBegins(validityBegins);
        } catch (JSONException e) {
            // We just take the default values, whenever parameters do
            // not exist or may be corrupt.
        }
    }

    private void addValidUntilParameter(JSONObject actionObject, RelutionAction action) {
        try {
            long validUntilInMs = actionObject.getLong(RelutionAction.VALID_UNTIL_PARAMETER) * 1000;
            long nowInMs = new Date().getTime();
            Date validityEnds = new Date(nowInMs + validUntilInMs);
            action.setValidityEnds(validityEnds);
        } catch (JSONException e) {
            // We just take the default values, whenever parameters do
            // not exist or may be corrupt.
        }
    }

    private void addRepeatEveryParameter(JSONObject actionObject, RelutionAction action) {
        try {
            long repeatEveryInMs = actionObject.getLong(RelutionAction.REPEAT_EVERY_PARAMETER) * 1000;
            // We only want to enable action locking, if repeatEveryInMs > 0.
            if (repeatEveryInMs > 0) {
                action.setLockingAction(true);
                action.setReleaseLockAfterMs(repeatEveryInMs);
            }
        } catch (JSONException e) {
            // We just take the default values, whenever parameters do
            // not exist or may be corrupt.
        }
    }

    private void addDistanceThresholdParameter(JSONObject actionObject, RelutionAction action) {
        try {
            int distanceThreshold = actionObject.getInt(RelutionAction.DISTANCE_THRESHOLD_PARAMETER);
            action.setDistanceThreshold(distanceThreshold);
        } catch (JSONException e) {
            // We just take the default values, whenever parameters do
            // not exist or may be corrupt.
        }
    }

    protected abstract RelutionAction createActionFromJSONIfPossible(JSONObject actionObject, BeaconMessage message) throws JSONException;
}
