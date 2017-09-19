//
//  RelutionStub.java
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
