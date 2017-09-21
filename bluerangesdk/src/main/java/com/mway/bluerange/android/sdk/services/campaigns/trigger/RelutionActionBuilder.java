//
//  RelutionActionBuilder.java
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
