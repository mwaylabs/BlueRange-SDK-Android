//
//  RelutionStub.java
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

package com.mway.bluerange.android.sdk.services.relution;

import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionInformation;
import com.mway.bluerange.android.sdk.services.relution.model.AdvertisingMessagesConfiguration;
import com.mway.bluerange.android.sdk.services.relution.model.RelutionTagInfos;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Basic implementation of the Relution client interface used for offline scenarios.
 */
public class RelutionStub implements Relution {

    @Override
    public boolean isServerAvailable() {
        // Always true for offline mode.
        return true;
    }

    @Override
    public AdvertisingMessagesConfiguration getAdvertisingMessagesConfiguration() throws Exception {
        return new AdvertisingMessagesConfiguration(new JSONArray());
    }

    @Override
    public void sendAnalyticsReport(JSONObject jsonReport) throws Exception {
        // Nothing to do
    }

    @Override
    public void setCalibratedRssiForIBeacon(IBeacon iBeacon, int calibratedRssi) throws Exception {
        // Nothing to do
    }

    @Override
    public RelutionActionInformation getActionsForIBeacon(IBeacon iBeacon) throws Exception {
        return new RelutionActionInformation();
    }

    @Override
    public RelutionTagInfos getRelutionTagInfos() throws Exception {
        return new RelutionTagInfos(new JSONArray());
    }

    @Override
    public String getOrganizationUuid() {
        return UUID.randomUUID().toString();
    }
}
