//
//  IBeaconMessage.java
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
