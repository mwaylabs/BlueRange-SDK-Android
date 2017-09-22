//
//  IBeaconMessage.java
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import org.altbeacon.beacon.Beacon;

import java.util.UUID;

/**
 * Implementation of Apple's IBeacon Bluetooth Low Energy messages.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("IBeaconMessage")
public class IBeaconMessage extends BeaconMessage {

    private IBeacon IBeacon;

    // Default constructor necessary for JSON deserialization
    public IBeaconMessage() {}

    public IBeaconMessage(Beacon beacon) {
        super(beacon);
        IBeacon = new IBeacon(beacon.getId1().toUuid(), beacon.getId2().toInt(), beacon.getId3().toInt());
    }

    public IBeaconMessage(UUID uuid, int major, int minor) {
        super();
        IBeacon = new IBeacon(uuid, major, minor);
    }

    @Override
    protected String getDescription() {
        String str = "";
        UUID uuid = this.getUUID();
        int major = this.getMajor();
        int minor = this.getMinor();
        String outputString = "IBeacon: UUID = " + uuid + ", major = " + major + ", minor = " + minor;
        return str + outputString;
    }

    @Override
    protected BeaconMessage copy() {
        IBeaconMessage clonedMessage = new IBeaconMessage(this.getUUID(), this.getMajor(), this.getMinor());
        return clonedMessage;
    }

    @Override
    public int hashCode() {
        return this.getUUID().hashCode() + this.getMajor() + this.getMinor();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IBeaconMessage)) {
            return false;
        }
        IBeaconMessage beaconMessage = (IBeaconMessage) o;
        return beaconMessage.getIBeacon().equals(this.getIBeacon());
    }

    @JsonIgnore
    public UUID getUUID() {
        return this.IBeacon.getUuid();
    }

    @JsonIgnore
    public int getMajor() {
        return this.IBeacon.getMajor();
    }

    @JsonIgnore
    public int getMinor() {
        return this.IBeacon.getMinor();
    }

    public IBeacon getIBeacon() {
        return IBeacon;
    }

    public void setIBeacon(IBeacon iBeacon) {
        this.IBeacon = iBeacon;
    }
}
