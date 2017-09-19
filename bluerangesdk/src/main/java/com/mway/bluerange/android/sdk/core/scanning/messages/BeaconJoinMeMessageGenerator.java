//
//  BeaconJoinMeMessageGenerator.java
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
 * A generator for the beacon join me messages.
 */
public class BeaconJoinMeMessageGenerator implements BeaconMessageGenerator {

    private static final long MESSAGE_TYPE_JOIN_ME = 1;
    private static final String MESH_IDENTIFIER = "f0";

    @Override
    public String getBeaconLayout() {
        // m = matching byte sequence: first and second byte
        //     should be the company identifier 0x4d02
        //     the third byte should be the meshId=f0
        //     because it is a message sent by a mesh beacon
        // i = most important identifier:
        //      is set to the company identifier
        //      second most is set to the messageType -> JOINME
        // p = txPower
        //      18th byte of the advertising message
        // d = dataField
        //      The tag data should be parsed out to the dataField.
        //      Byte 5-25

        String companyIdentifierHexString = Integer.toHexString(Constants.MWAY_COMPANY_IDENTIFIER);
        int dataStartIndex = 2;
        int dataLength = 24;
        String layout = "m:0-2=" + companyIdentifierHexString + MESH_IDENTIFIER + ",i:0-1,i:2-2,p:18-18";
        for(int i = 0; i < dataLength; i++) {
            int byteIndex = dataStartIndex+i;
            layout += ",d:" + byteIndex + "-" + byteIndex;
        }
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        // The region of the Relution tag message is always defined
        // by a fixed company identifier.
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
            // If no data fields exist, this is not a joint me message.
            if (dataFields.size() == 0) {
                return false;
            }
            // It is a join me message, if the message type == 1
            String meshIdentifier = beacon.getId2().toHexString().substring(2);
            long messageType = dataFields.get(3);
            return messageType == MESSAGE_TYPE_JOIN_ME && meshIdentifier.equals(MESH_IDENTIFIER);
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public BeaconJoinMeMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        BeaconJoinMeMessage beaconMessage = new BeaconJoinMeMessage(beacon, region);
        return beaconMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  BeaconJoinMeMessageGenerator)) {
            return false;
        }
        // All join me message generators are the same.
        return true;
    }
}
