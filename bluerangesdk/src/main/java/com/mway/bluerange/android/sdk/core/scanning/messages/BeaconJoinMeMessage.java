//
//  BeaconJoinMeMessage.java
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
 * Beacon join me messages are send by BlueRange SmartBeacons, whenever a beacon is able to
 * connect with another one. These messages contain useful information about a beacon which can
 * be used to identify a beacon. Moreover, since they are sent regularly by a beacon these
 * messages might be used for position estimation use cases like indoor navigation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("BeaconJoinMeMessage")
public class BeaconJoinMeMessage extends BeaconMessage {

    private transient /*u16*/ int networkId;
    private transient /*u16*/ int nodeId;
    private transient /*u32*/ long clusterId;
    private transient /*i16*/ short clusterSize;
    private transient /*u8*/ short freeInConnections;
    private transient /*u8*/ short freeOutConnections;
    // Version specific content
    private transient /*u8*/ short batteryRuntime;
    private transient /*i8*/ short txPower;
    private transient /*u8*/ short deviceType;
    private transient /*u16*/ int hopsToSink;
    private transient /*u16*/ int meshWriteHandle;
    private transient /*u32*/ long ackField;

    public BeaconJoinMeMessage(Beacon beacon, Region region) {
        super(beacon);
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
        int meshIdentifier = parser.readSwappedByte(bytes);
        // BlueRange advertising message header
        this.networkId = parser.readSwappedUnsignedShort(bytes);
        int messageType = parser.readSwappedByte(bytes);

        // Join me payload
        this.nodeId = parser.readSwappedUnsignedShort(bytes);
        this.clusterId = parser.readSwappedUnsignedInteger(bytes);
        this.clusterSize = parser.readSwappedShort(bytes);
        this.freeInConnections = parser.readSwappedBitsOfByteWithLockedPointer(bytes, 0, 2);
        this.freeOutConnections = parser.readSwappedBitsOfByte(bytes, 3, 7);
        this.batteryRuntime = parser.readSwappedUnsignedByte(bytes);
        this.txPower = parser.readSwappedUnsignedByte(bytes);
        this.deviceType = parser.readSwappedUnsignedByte(bytes);
        this.hopsToSink = parser.readSwappedShort(bytes);
        this.meshWriteHandle = parser.readSwappedShort(bytes);
        this.ackField = parser.readSwappedUnsignedInteger(bytes);
    }

    // Default constructor necessary for JSON deserialization
    public BeaconJoinMeMessage() {}

    public BeaconJoinMeMessage(int sender, long clusterId, short clusterSize, short freeInConnections,
                               short freeOutConnections, short batteryRuntime, short txPower, short deviceType,
                               int hopsToSink, int meshWriteHandle, long ackField) {
        super();
        init(sender, clusterId, clusterSize, freeInConnections, freeOutConnections, batteryRuntime,
                txPower, deviceType, hopsToSink, meshWriteHandle, ackField);
    }

    public BeaconJoinMeMessage(Date date, int sender, long clusterId, short clusterSize, short freeInConnections,
                               short freeOutConnections, short batteryRuntime, short txPower, short deviceType,
                               int hopsToSink, int meshWriteHandle, long ackField) {
        super(date);
        init(sender, clusterId, clusterSize, freeInConnections, freeOutConnections, batteryRuntime,
                txPower, deviceType, hopsToSink, meshWriteHandle, ackField);
    }

    private void init(int sender, long clusterId, short clusterSize, short freeInConnections,
                      short freeOutConnections,
                      short batteryRuntime, short txPower, short deviceType,
                      int hopsToSink, int meshWriteHandle, long ackField) {
        this.nodeId = sender;
        this.clusterId = clusterId;
        this.clusterSize = clusterSize;
        this.freeInConnections = freeInConnections;
        this.freeOutConnections = freeOutConnections;
        this.batteryRuntime = batteryRuntime;
        this.txPower = txPower;
        this.deviceType = deviceType;
        this.hopsToSink = hopsToSink;
        this.meshWriteHandle = meshWriteHandle;
        this.ackField = ackField;
    }

    @Override
    public int hashCode() {
        return this.getNodeId();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeaconJoinMeMessage)) {
            return false;
        }
        BeaconJoinMeMessage beaconMessage = (BeaconJoinMeMessage) o;
        return beaconMessage.getNodeId() == this.getNodeId();
    }

    @Override
    protected String getDescription() {
        String str = "";
        str += "BeaconJoinMeMessage:";
        str += "nodeId: " + this.nodeId + ", ";
        str += "rssi: " + this.getRssi();
        return str;
    }

    @Override
    protected BeaconMessage copy() {
        BeaconJoinMeMessage clonedMessage = new BeaconJoinMeMessage(
                nodeId,
                clusterId,
                clusterSize,
                freeInConnections,
                freeOutConnections,
                batteryRuntime,
                txPower,
                deviceType,
                hopsToSink,
                meshWriteHandle,
                ackField);
        return clonedMessage;
    }

    // Getters and setters


    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public short getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(short clusterSize) {
        this.clusterSize = clusterSize;
    }

    public short getFreeInConnections() {
        return freeInConnections;
    }

    public void setFreeInConnections(short freeInConnections) {
        this.freeInConnections = freeInConnections;
    }

    public short getFreeOutConnections() {
        return freeOutConnections;
    }

    public void setFreeOutConnections(short freeOutConnections) {
        this.freeOutConnections = freeOutConnections;
    }

    public short getBatteryRuntime() {
        return batteryRuntime;
    }

    public void setBatteryRuntime(short batteryRuntime) {
        this.batteryRuntime = batteryRuntime;
    }

    @Override
    public short getTxPower() {
        return txPower;
    }

    @Override
    public void setTxPower(short txPower) {
        this.txPower = txPower;
    }

    public short getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(short deviceType) {
        this.deviceType = deviceType;
    }

    public int getHopsToSink() {
        return hopsToSink;
    }

    public void setHopsToSink(int hopsToSink) {
        this.hopsToSink = hopsToSink;
    }

    public int getMeshWriteHandle() {
        return meshWriteHandle;
    }

    public void setMeshWriteHandle(int meshWriteHandle) {
        this.meshWriteHandle = meshWriteHandle;
    }

    public long getAckField() {
        return ackField;
    }

    public void setAckField(long ackField) {
        this.ackField = ackField;
    }
}
