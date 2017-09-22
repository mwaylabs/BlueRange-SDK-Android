//
//  RelutionTagMessageGeneratorV1.java
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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mway.bluerange.android.sdk.common.Constants;
import com.mway.bluerange.android.sdk.utils.string.IntToHexStringConverter;

/**
 * A message generator for Relution Tag messages.
 */
public class RelutionTagMessageGeneratorV1 implements BeaconMessageGenerator, Serializable {

    private static final String MESSAGE_TYPE = IntToHexStringConverter.
            intToHexStringWithLeadingZero(RelutionTagMessageV1.RELUTION_TAG_MESSAGE_TYPE);

    private boolean tagFilteringEnabled = false;
    private long[] tags;

    public RelutionTagMessageGeneratorV1() {
        this.tagFilteringEnabled = false;
    }

    public RelutionTagMessageGeneratorV1(long[] tags) {
        this.tags = tags;
        this.tagFilteringEnabled = true;
    }

    @Override
    public String getBeaconLayout() throws Exception {
        // m = matching byte sequence: first and second byte
        //     should be the company identifier 0x4d02
        //     the third byte should be the advertising message type.
        // i = most important identifier:
        //      is set to the company identifier
        // p = txPower
        //      third byte of the tag message
        // d = dataField
        //      The tag data should be parsed out to the dataField.
        //      Byte 5-25
        int tagDataStartIndex = 5;
        int tagDataLength = 21;
        String companyIdentifierHexString = Integer.toHexString(Constants.MWAY_COMPANY_IDENTIFIER);
        String layout = "m:0-2=" + companyIdentifierHexString + MESSAGE_TYPE + ",i:0-1,i:2-2,p:3-3";
        for(int i = 0; i < tagDataLength; i++) {
            int byteIndex = tagDataStartIndex+i;
            layout += ",d:" + byteIndex + "-" + byteIndex;
        }
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        // The region of the Relution tag message is always defined
        // by a fixed company identifier.
        int id1 = Constants.MWAY_COMPANY_IDENTIFIER;
        //int id2 = Constants.RELUTION_TAG_MESSAGE_TYPE;
        Region region = new Region(this.toString(),
                Identifier.fromInt(id1), null, null);
        return region;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        try {
            String messageType = beacon.getId2().toString();
            String expectedMessageType = "0x" + MESSAGE_TYPE;
            if (!messageType.equals(expectedMessageType)) {
                return false;
            }
            if (tagFilteringEnabled) {
                boolean isValidBeacon = false;
                try {
                    // If at least one tag matches, the beacon
                    // conforms to the message filter.
                    List<Long> tagList = getTags(beacon);
                    for (int i = 0; i < this.tags.length; i++) {
                        if (tagList.contains(this.tags[i])) {
                            isValidBeacon = true;
                            break;
                        }
                    }
                } catch(Exception e) {
                    isValidBeacon = false;
                }
                return isValidBeacon;
            } else {
                return true;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    private List<Long> getTags(Beacon beacon) {
        // Get access to the underlying data fields of the beacon.
        List<Long> dataFields = beacon.getDataFields();

        // Extract the tags from the beacon object.
        List<Long> tagList = new ArrayList<Long>();
        int i = 0;
        while(i < dataFields.size()) {
            long byte1 = (dataFields.get(i).longValue());
            long byte2 = (dataFields.get(i+1).longValue());
            long byte3 = (dataFields.get(i+2).longValue());
            long tag = (byte1<<0) + (byte2<<8) + (byte3<<16);
            // Do not add tags with value 0x00
            if (tag != 0x00) {
                tagList.add(tag);
            }
            i += 3;
        }
        return tagList;
    }

    @Override
    public RelutionTagMessageV1 constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        List<Long> tagList = getTags(beacon);
        RelutionTagMessageV1 beaconMessage = new RelutionTagMessageV1(beacon, region, tagList);
        return beaconMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelutionTagMessageGeneratorV1)) {
            return false;
        }
        RelutionTagMessageGeneratorV1 generator = (RelutionTagMessageGeneratorV1) o;
        // If at least one of the generators filters all Relution tags, it is considered
        // to be "the same" generator since it encloses the other one.
        if ((this.tagFilteringEnabled && generator.tagFilteringEnabled)
                && !tagsAreSame(generator.tags, this.tags)) {
            return false;
        }
        return true;
    }

    private boolean tagsAreSame(long[] tags1, long[] tags2) {
        if (tags1.length != tags2.length) {
            return false;
        }
        for (int i = 0; i < tags1.length; i++) {
            long tag1 = tags1[i];
            boolean containsTag1 = false;
            for (int j = 0; j < tags2.length; j++) {
                if (tag1 == tags2[j]) {
                    containsTag1 = true;
                }
            }
            if (!containsTag1) {
                return false;
            }
        }
        return true;
    }
}
