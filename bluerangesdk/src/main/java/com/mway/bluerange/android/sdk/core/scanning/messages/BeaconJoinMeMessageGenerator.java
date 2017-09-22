//
//  BeaconJoinMeMessageGenerator.java
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
