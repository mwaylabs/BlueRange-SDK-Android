//
//  EddystoneMessage.java
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

import org.altbeacon.beacon.Beacon;

/**
 * Abstract base class of all Eddystone messages.
 */
public abstract class EddystoneMessage extends BeaconMessage {

    private transient /*u16*/ int eddystoneUUID;
    private transient /*u8*/ int serviceDataLength;
    private transient /*u8*/ int serviceDataType;
    private transient /*u16*/ int eddystoneUUID2;
    private transient /*i8*/ short txPower;

    public EddystoneMessage() {
        super();
    }

    public EddystoneMessage(Beacon beacon) {
        super(beacon);
    }

    // Getters and setters

    public int getEddystoneUUID() {
        return eddystoneUUID;
    }

    public void setEddystoneUUID(int eddystoneUUID) {
        this.eddystoneUUID = eddystoneUUID;
    }

    public int getServiceDataLength() {
        return serviceDataLength;
    }

    public void setServiceDataLength(int serviceDataLength) {
        this.serviceDataLength = serviceDataLength;
    }

    public int getServiceDataType() {
        return serviceDataType;
    }

    public void setServiceDataType(int serviceDataType) {
        this.serviceDataType = serviceDataType;
    }

    public int getEddystoneUUID2() {
        return eddystoneUUID2;
    }

    public void setEddystoneUUID2(int eddystoneUUID2) {
        this.eddystoneUUID2 = eddystoneUUID2;
    }

    @Override
    public short getTxPower() {
        return txPower;
    }

    @Override
    public void setTxPower(short txPower) {
        this.txPower = txPower;
    }
}
