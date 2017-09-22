//
//  RelutionIBeaconMessageActionMapper.java
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
