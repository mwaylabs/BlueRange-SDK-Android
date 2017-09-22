//
//  AssetTrackingMessageV1Generator.java
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

import com.mway.bluerange.android.sdk.common.Constants;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.List;

/**
 * A message generator parsing AssetTrackingMessageV1 messages.
 */
public class AssetTrackingMessageV1Generator implements BeaconMessageGenerator {

    private static final String ASSET_V1_TYPE = "02";
    private int assetId;
    private boolean assetIdFilteringEnabled = false;

    // Initialization

    public AssetTrackingMessageV1Generator() {
        this.assetIdFilteringEnabled = false;
    }

    public AssetTrackingMessageV1Generator(int assetId) {
        this.assetId = assetId;
        this.assetIdFilteringEnabled = true;
    }

    @Override
    public String getBeaconLayout() throws Exception {
        // m = matching byte sequence: first and second byte
        //     should be the company identifier 0x4d02
        //     the third byte should be asset V1 type = 0x02
        // i = most important identifier:
        //      is set to the company identifier
        //      second most is set to the messageType -> Asset V1 type = 0x02
        // d = dataField
        //      The asset id should be parsed out of the data field
        // p = txPower
        //      Not valid in this case, but necessary to get android beacon library scanner run...

        String companyIdentifierHexString = Integer.toHexString(Constants.MWAY_COMPANY_IDENTIFIER);
        int dataStartIndex = 3;
        int dataLength = 2;
        String layout = "m:0-2=" + companyIdentifierHexString + ASSET_V1_TYPE + ",i:0-1,i:2-2,p:2-2";
        for(int i = 0; i < dataLength; i++) {
            int byteIndex = dataStartIndex+i;
            layout += ",d:" + byteIndex + "-" + byteIndex;
        }
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        int id1 = Constants.MWAY_COMPANY_IDENTIFIER;
        Region region = new Region(this.toString(),
                Identifier.fromInt(id1), null, null);
        return region;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        try {
            // Get access to the underlying data fields of the beacon.
            List<Long> dataFields = beacon.getDataFields();
            // If no data fields exist, this is not an asset tracking message.
            if (dataFields.size() == 0) {
                return false;
            }
            // Company identifier must fit
            String expectedCompanyIdentifierHexString = Integer.toHexString(Constants.MWAY_COMPANY_IDENTIFIER);
            String companyIdentifierHexString = beacon.getId1().toHexString().substring(2);

            // Message type must fit
            String expectedMessageType = ASSET_V1_TYPE;
            String messageType = beacon.getId2().toHexString().substring(2);
            boolean valid = expectedCompanyIdentifierHexString.equals(companyIdentifierHexString)
                && expectedMessageType.equals(messageType);
            return valid;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public BeaconMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        AssetTrackingMessageV1 beaconMessage = new AssetTrackingMessageV1(beacon, region);
        return beaconMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  AssetTrackingMessageV1Generator)) {
            return false;
        }
        AssetTrackingMessageV1Generator generator = (AssetTrackingMessageV1Generator) o;
        if (this.assetIdFilteringEnabled && (generator.getAssetId() != getAssetId())) {
            return false;
        }
        return true;
    }

    // Getters and setters

    public int getAssetId() {
        return this.assetId;
    }
}
