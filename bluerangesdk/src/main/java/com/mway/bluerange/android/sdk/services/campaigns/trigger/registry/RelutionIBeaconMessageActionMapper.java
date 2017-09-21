//
//  RelutionIBeaconMessageActionMapper.java
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

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionRegistry;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionInformation;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionRegistry;
import com.mway.bluerange.android.sdk.services.relution.Relution;
import com.mway.bluerange.android.sdk.utils.network.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.utils.URIBuilder;

public class RelutionIBeaconMessageActionMapper implements IBeaconMessageActionMapper {

    private Relution relution;

    public RelutionIBeaconMessageActionMapper(Relution relution) {
        this.relution = relution;
    }

    @Override
    public RelutionActionInformation getBeaconActionInformation(IBeaconMessage message)
            throws
            RelutionActionRegistry.RegistryNotAvailableException,
            RelutionActionRegistry.UnsupportedMessageException {
        try {
            IBeacon iBeacon = message.getIBeacon();
            RelutionActionInformation actionInformation = relution.getActionsForIBeacon(iBeacon);
            return actionInformation;
        } catch (Exception e) {
            throw new BeaconActionRegistry.RegistryNotAvailableException();
        }
    }

    @Override
    public boolean isAvailable() {
        return relution.isServerAvailable();
    }
}
