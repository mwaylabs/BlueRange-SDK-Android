//
//  EddystoneUrlMessageGenerator.java
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

package com.mway.bluerange.android.sdk.core.scanning.messages;

import com.mway.bluerange.android.sdk.utils.string.ByteArrayConverter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.List;

/**
 * The beacon message generator class for Eddystone URL messages.
 */
public class EddystoneUrlMessageGenerator extends EddystoneMessageGenerator {

    private boolean urlFilteringEnabled = false;
    private String url = null;

    public EddystoneUrlMessageGenerator() {
        this.urlFilteringEnabled = false;
    }

    public EddystoneUrlMessageGenerator(String url) {
        this.urlFilteringEnabled = true;
        this.url = url;
    }

    @Override
    public String getBeaconLayout() throws Exception {
        String layout = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v,d:0-1,d:2-2";
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        Identifier urlIdentifier;

        if (this.urlFilteringEnabled) {
            byte[] urlBytes = EddystoneUrlMessage.getUrlBytesFromString(this.url);
            urlIdentifier = Identifier.fromBytes(
                    urlBytes, 0, urlBytes.length, false);
        } else {
            urlIdentifier = null;
        }

        Region region = new Region(this.toString(), urlIdentifier, null, null);
        return region;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        boolean isValidBeacon = super.isValidBeacon(beacon);

        // Message must be an Eddystone message.
        if (!isValidBeacon) {
            return false;
        }

        try {
            // Message must be of type Eddystone URL
            List<Long> dataFields = beacon.getDataFields();
            long eddystoneFrameType = dataFields.get(1);
            if (eddystoneFrameType != EDDY_FRAME_URL) {
                return false;
            }

            String actualUrl = EddystoneUrlMessage.getUrlStringFromBytes(beacon.getId1().toByteArray());
            String acceptedUrl = this.url;

            isValidBeacon = true;

            if (urlFilteringEnabled && (!actualUrl.equals(acceptedUrl))) {
                isValidBeacon = false;
            }
        } catch(Exception e) {
            isValidBeacon = false;
        }
        return isValidBeacon;
    }

    @Override
    public EddystoneUrlMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        EddystoneUrlMessage eddystoneUrlMessage = new EddystoneUrlMessage(beacon);
        return eddystoneUrlMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  EddystoneUrlMessageGenerator)) {
            return false;
        }
        EddystoneUrlMessageGenerator generator = (EddystoneUrlMessageGenerator) o;

        if ((this.urlFilteringEnabled && generator.urlFilteringEnabled)
                && (!generator.url.equals(this.url))) {
            return false;
        }

        return true;
    }
}
