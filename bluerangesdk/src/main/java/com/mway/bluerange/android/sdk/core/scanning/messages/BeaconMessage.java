//
//  BeaconMessage.java
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
