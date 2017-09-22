//
//  EddystoneMessage.java
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
