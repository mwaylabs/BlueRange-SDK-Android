//
//  RelutionScanConfigLoaderStub.java
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

package com.mway.bluerange.android.sdk.services.configuration;

import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.IBeaconMessageActionMapperStub;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class RelutionScanConfigLoaderStub implements RelutionScanConfigLoader {

    private IBeaconMessageActionMapperStub mapperStub;

    public RelutionScanConfigLoaderStub(IBeaconMessageActionMapperStub mapperStub) {
        this.mapperStub = mapperStub;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public List<UUID> getUuids() throws Exception {
        List<UUID> uuids = new ArrayList<>();
        Map<IBeacon, JSONObject> iBeaconActionMap = mapperStub.getIBeaconActionMap();
        Set<IBeacon> iBeacons = iBeaconActionMap.keySet();
        for (IBeacon iBeacon : iBeacons) {
            uuids.add(iBeacon.getUuid());
        }
        return uuids;
    }

}
