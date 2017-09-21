//
//  Relution.java
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
