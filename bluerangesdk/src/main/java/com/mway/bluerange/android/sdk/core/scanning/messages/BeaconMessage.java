//
//  BeaconMessage.java
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
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.altbeacon.beacon.Beacon;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The base class of all beacon messages.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=AssetTrackingMessageV1.class, name="AssetTrackingMessageV1"),
        @JsonSubTypes.Type(value=BeaconJoinMeMessage.class, name="BeaconJoinMeMessage"),
        @JsonSubTypes.Type(value=EddystoneUidMessage.class, name="EddystoneUidMessage"),
        @JsonSubTypes.Type(value=EddystoneUrlMessage.class, name="EddystoneUrlMessage"),
        @JsonSubTypes.Type(value=IBeaconMessage.class, name="IBeaconMessage"),
        @JsonSubTypes.Type(value=RelutionTagMessage.class, name="RelutionTagMessage"),
        @JsonSubTypes.Type(value=RelutionTagMessageV1.class, name="RelutionTagMessageV1"),
        @JsonSubTypes.Type(value=NullBeaconMessage.class, name="NullBeaconMessage"),
})
public abstract class BeaconMessage implements Serializable {

    @JsonIgnore
    private transient Beacon beacon;
    private Date timestamp;
    public static final short DEFAULT_TX_POWER = -55;
    private short txPower = DEFAULT_TX_POWER;
    private int rssi = -70;

    public BeaconMessage() {
        this.init(null, new Date());
    }

    public BeaconMessage(Beacon beacon) {
        this.init(beacon, new Date());
    }

    public BeaconMessage(Date timestamp) {
        this.init(null, timestamp);
    }

    public BeaconMessage(Beacon beacon, Date timestamp) {
        this.init(beacon, timestamp);
    }

    private void init(Beacon beacon, Date timestamp) {
        this.timestamp = timestamp;
        this.beacon = beacon;
        // beacon fields
        if (beacon != null) {
            this.txPower = (short)beacon.getTxPower();
            this.rssi = beacon.getRssi();
        }
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object other);

    public String toString() {
        DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
        String dateString = format.format(timestamp);
        String str = "[" + dateString + "]: ";
        str += getDescription();
        return str;
    }

    protected abstract String getDescription();
    public BeaconMessage clone() {
        BeaconMessage message = copy();
        message.setRssi(rssi);
        message.setTxPower(txPower);
        message.setTimestamp(timestamp);
        return message;
    }

    protected abstract BeaconMessage copy();

    // Getters and setters

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public short getTxPower() {
        return txPower;
    }

    public void setTxPower(short txPower) {
        this.txPower = txPower;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    // The beacon message type
    public String getType() {
        return this.getClass().getSimpleName();
    }


}
