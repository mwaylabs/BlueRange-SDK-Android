//
//  RelutionTagMessageGenerator.java
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
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

public class RelutionTagMessageGenerator extends EddystoneUidMessageGenerator {

    private boolean tagFilteringEnabled = false;
    private int[] tags;

    public RelutionTagMessageGenerator(String namespaceUid) {
        super(namespaceUid);
        this.tagFilteringEnabled = false;
    }

    public RelutionTagMessageGenerator(String namespaceUid, int[] tags) throws Exception {
        super(namespaceUid);
        this.tags = tags;
        this.tagFilteringEnabled = true;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        boolean isEddystoneUidValid = super.isValidBeacon(beacon);
        if (!isEddystoneUidValid) {
            return false;
        }
        // Check if at least on tag matches to the filter.
        if (tagFilteringEnabled) {
            boolean isValidBeacon = false;
            try {
                // If at least one tag matches, the beacon message
                // passes the filter.
                RelutionTagMessage message = new RelutionTagMessage(beacon);
                List<Integer> tagList = message.getTags();
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
    }

    @Override
    public EddystoneUidMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        RelutionTagMessage relutionTagMessage = new RelutionTagMessage(beacon);
        return relutionTagMessage;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEddystoneUidEqual =  super.equals(o);
        if (!isEddystoneUidEqual) {
            return false;
        }
        if (!(o instanceof RelutionTagMessageGenerator)) {
            return false;
        }
        // Check if tags are equal.
        RelutionTagMessageGenerator generator = (RelutionTagMessageGenerator) o;
        // If at least one of the generators filters all Relution tags, it is considered
        // to be "the same" generator since it encloses the other one.
        if ((this.tagFilteringEnabled && generator.tagFilteringEnabled)
                && !tagsAreSame(generator.tags, this.tags)) {
            return false;
        }
        return true;
    }

    private boolean tagsAreSame(int[] tags1, int[] tags2) {
        if (tags1.length != tags2.length) {
            return false;
        }
        for (int i = 0; i < tags1.length; i++) {
            int tag1 = tags1[i];
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
