//
//  AssetTrackingMessageV1.java
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mway.bluerange.android.sdk.utils.structs.ByteArrayParser;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.Date;
import java.util.List;

/**
 * AssetTracking messages are broadcasted by Relution SmartAssets.
 * SmartBeacons will collect these messages to do indoor localization using the RSSI measurements.
 * The message only contains an asset identifier to map the RSSI values to a thing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("AssetTrackingMessageV1")
public class AssetTrackingMessageV1 extends BeaconMessage {

    private int assetId;

    // Default constructor necessary for JSON deserialization
    public AssetTrackingMessageV1() {}

    public AssetTrackingMessageV1(Beacon beacon, Region region) {
        super(beacon);
        // Actually the Asset tracking message does not have this field.
        // So the wrong value will be set by the beacon, because the AltBeacon library
        // requires the txPower field.
        setTxPower(DEFAULT_TX_POWER);
        initWithBeacon(beacon);
    }

    private void initWithBeacon(Beacon beacon) {
        byte[] bytes = getBytes(beacon);
        initWithBytes(bytes);
    }

    private byte[] getBytes(Beacon beacon) {
        List<Long> dataFields = beacon.getDataFields();
        byte[] bytes = new byte[dataFields.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)((long)dataFields.get(i));
        }
        return bytes;
    }

    private void initWithBytes(byte[] bytes) {
        ByteArrayParser parser = new ByteArrayParser(0);
        this.assetId = parser.readSwappedShort(bytes);
    }

    public AssetTrackingMessageV1(int assetId) {
        super();
        init(assetId);
    }

    private void init(int assetId) {
        this.assetId = assetId;
    }

    @Override
    public int hashCode() {
        return this.getAssetId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AssetTrackingMessageV1)) {
            return false;
        }
        AssetTrackingMessageV1 beaconMessage = (AssetTrackingMessageV1) o;
        return beaconMessage.getAssetId() == this.getAssetId();
    }

    @Override
    protected String getDescription() {
        String str = "";
        str += "AssetTrackingMessageV1:";
        str += "nodeId: " + this.assetId + ", ";
        str += "rssi: " + this.getRssi();
        return str;
    }

    @Override
    protected BeaconMessage copy() {
        AssetTrackingMessageV1 clonedMessage = new AssetTrackingMessageV1(assetId);
        return clonedMessage;
    }

    // Getters and setters
    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }
}
