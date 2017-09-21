//
//  AssetTrackingMessageV1Generator.java
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
