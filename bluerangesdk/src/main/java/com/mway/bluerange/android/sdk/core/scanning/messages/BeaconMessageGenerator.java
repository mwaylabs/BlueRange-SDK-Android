//
//  BeaconMessageGenerator.java
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
import org.altbeacon.beacon.Region;

/**
 * The beacon message generator is used to filter and generate {@link BeaconMessage} objects out
 * of {@link Beacon} objects.
 */
public interface BeaconMessageGenerator {
    // Operations needed to configure the beacon manager of
    // the android beacon library used to filter out
    // mesh beacon messages.
    String getBeaconLayout() throws Exception;
    Region getRegion() throws Exception;
    // Operation used to do a more precise filtering.
    boolean isValidBeacon(Beacon beacon);
    // Needed to construct the appropriate beacon message.
    BeaconMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception;
    boolean equals(Object o);
}
