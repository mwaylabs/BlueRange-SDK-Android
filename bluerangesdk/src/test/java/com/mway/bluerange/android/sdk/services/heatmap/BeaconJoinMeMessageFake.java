//
//  BeaconJoinMeMessageFake.java
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

package com.mway.bluerange.android.sdk.services.heatmap;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconJoinMeMessage;

import java.util.Date;

/**
 *
 */
public class BeaconJoinMeMessageFake extends BeaconJoinMeMessage {

    private int rssi;

    public BeaconJoinMeMessageFake(Date date, int rssi, int sender, long clusterId,
                                   short clusterSize, short freeInConnections,
                                   short freeOutConnections, short batteryRuntime,
                                   short txPower, short deviceType, int hopsToSink,
                                   int meshWriteHandle, int ackField) {
        super(date, sender, clusterId, clusterSize, freeInConnections, freeOutConnections,
                batteryRuntime, txPower, deviceType, hopsToSink,
                meshWriteHandle, ackField);
        this.setRssi(rssi);
    }

    @Override
    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
