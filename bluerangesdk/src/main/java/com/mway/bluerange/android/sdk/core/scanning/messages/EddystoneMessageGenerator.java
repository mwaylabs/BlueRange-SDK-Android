//
//  EddystoneMessageGenerator.java
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

import java.util.List;

public abstract class EddystoneMessageGenerator implements BeaconMessageGenerator {

    public static final int EDDYSTONE_SERVICE_UUID = 0xAAFE;

    public static int EDDY_FRAME_UID = Integer.parseInt("00000000", 2);
    public static int EDDY_FRAME_URL = Integer.parseInt("00010000", 2);
    public static int EDDY_FRAME_TLM = Integer.parseInt("00100000", 2);
    public static int EDDY_FRAME_EID = Integer.parseInt("01000000", 2);

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        try {
            List<Long> dataFields = beacon.getDataFields();
            long serviceUuid = dataFields.get(0);

            if (serviceUuid != EDDYSTONE_SERVICE_UUID) {
                return false;
            }
            return true;

        } catch (Throwable t) {
            return false;
        }
    }
}
