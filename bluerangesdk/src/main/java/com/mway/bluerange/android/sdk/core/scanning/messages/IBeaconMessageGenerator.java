//
//  IBeaconMessageGenerator.java
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

import android.annotation.SuppressLint;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.io.Serializable;
import java.util.UUID;

/**
 * A message generator for iBeacon messages.
 */
public class IBeaconMessageGenerator implements BeaconMessageGenerator, Serializable {

    private String uuid;
    private boolean majorFilteringEnabled = false;
    private int major;
    private boolean minorFilteringEnabled = false;
    private int minor;

    public IBeaconMessageGenerator(String uuid) {
        this.uuid = uuid;
        this.majorFilteringEnabled = false;
        this.minorFilteringEnabled = false;
    }

    public IBeaconMessageGenerator(String uuid, int major) {
        this.uuid = uuid;
        this.major = major;
        this.majorFilteringEnabled = true;
        this.minorFilteringEnabled = false;
    }

    public IBeaconMessageGenerator(String uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.majorFilteringEnabled = true;
        this.minorFilteringEnabled = true;
    }

    public String getUuid() {
        return uuid;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    @SuppressLint("DefaultLocale")
    public String getBeaconLayout() throws Exception {
        Region region = getRegion();
        String uuid = region.getId1().toHexString();
        // e.g. "0xb9407f30f5f8466eaff925556b57fe6d" -> extract -> "f5f846"
        int patternLength = 6;
        int patternStartIndex = 10;
        int patternEndIndex = patternStartIndex + patternLength;
        String uuidPattern = uuid.substring(patternStartIndex, patternEndIndex);
        String layout = "m:8-10=" + uuidPattern.toUpperCase() + ",i:4-19,i:20-21,i:22-23,p:24-24";
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        Identifier uuidId = Identifier.parse(this.uuid);
        Identifier majorId;
        Identifier minorId;

        if (majorFilteringEnabled) {
            majorId = Identifier.fromInt(this.major);
        } else {
            majorId = null;
        }

        if (minorFilteringEnabled) {
            minorId = Identifier.fromInt(this.minor);
        } else {
            minorId = null;
        }

        Region region = new Region(this.toString(), uuidId, majorId, minorId);
        return region;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        boolean isValidBeacon = false;
        try {
            UUID uuid = UUID.fromString(beacon.getId1().toString());
            int major = beacon.getId2().toInt();
            int minor = beacon.getId3().toInt();

            UUID acceptedUuid = UUID.fromString(this.getUuid());
            int acceptedMajor = this.getMajor();
            int acceptedMinor = this.getMinor();

            isValidBeacon = uuid.equals(acceptedUuid);

            if (majorFilteringEnabled && (major != acceptedMajor)) {
                isValidBeacon = false;
            }

            if (minorFilteringEnabled && (minor != acceptedMinor)) {
                isValidBeacon = false;
            }
        } catch(Exception e) {
            isValidBeacon = false;
        }
        return isValidBeacon;
    }

    @Override
    public IBeaconMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        IBeaconMessage beaconMessage = new IBeaconMessage(beacon);
        return beaconMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  IBeaconMessageGenerator)) {
            return false;
        }
        IBeaconMessageGenerator generator = (IBeaconMessageGenerator) o;
        if (!generator.getUuid().equals(uuid)) {
            return false;
        }

        if ((this.majorFilteringEnabled && generator.majorFilteringEnabled) && (generator.getMajor() != getMajor())) {
            return false;
        }

        if ((minorFilteringEnabled && generator.minorFilteringEnabled) && (generator.getMinor() != getMinor())) {
            return false;
        }

        return true;
    }
}
