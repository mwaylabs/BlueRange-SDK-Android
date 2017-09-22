//
//  Relution.java
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

import org.json.JSONObject;

public interface Relution {

    // Classes
    class RelutionException extends Exception {}

    class LoginException extends Exception {}

    /**
     * Returns true, if Relution is available.
     * @return
     */
    boolean isServerAvailable();

    /**
     * Returns the current advertising messages configuration. This should be used
     * to configure the scanner to scan for the correct beacon messages.
     * @return
     * @throws Exception
     */
    AdvertisingMessagesConfiguration getAdvertisingMessagesConfiguration() throws Exception;

    /**
     * Sends an report to Relution containing the received beacons and their corresponding RSSI values.
     * @param jsonReport
     * @throws Exception
     */
    void sendAnalyticsReport(JSONObject jsonReport) throws Exception;

    /**
     * Calibrates the txPower field of an iBeacon.
     * @param iBeacon
     * @param calibratedRssi
     * @throws Exception
     */
    void setCalibratedRssiForIBeacon(IBeacon iBeacon, int calibratedRssi) throws Exception;

    /**
     * Returns the Campaign actions configured for the passed iBeacon message.
     * @param iBeacon
     * @return
     * @throws Exception
     */
    RelutionActionInformation getActionsForIBeacon(IBeacon iBeacon) throws Exception;

    /**
     * Returns all Relution tag information.
     * @return
     * @throws Exception
     */
    RelutionTagInfos getRelutionTagInfos() throws Exception;

    /**
     * Returns the organiation UUID
     * @return
     */
    String getOrganizationUuid();
}
