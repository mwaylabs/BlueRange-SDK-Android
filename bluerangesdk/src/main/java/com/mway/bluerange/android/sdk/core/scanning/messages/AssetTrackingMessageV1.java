//
//  AssetTrackingMessageV1.java
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
