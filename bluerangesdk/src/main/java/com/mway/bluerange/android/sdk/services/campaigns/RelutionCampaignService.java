//
//  RelutionCampaignService.java
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

package com.mway.bluerange.android.sdk.services.campaigns;

import android.content.Context;

import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerConfig;
import com.mway.bluerange.android.sdk.core.scanning.IBeaconMessageScanner;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionDebugListener;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionListener;
import com.mway.bluerange.android.sdk.core.triggering.BeaconMessageActionTrigger;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.IBeaconMessageActionMapper;



import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry
        .RelutionIBeaconMessageActionMapper;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.RelutionTagMessageActionMapperEmptyStub;


import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionRegistry;
import com.mway.bluerange.android.sdk.services.relution.Relution;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

import org.json.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A Relution campaign service can trigger actions defined in a Relution campaign. Internally
 * this class uses the {@link BeaconMessageActionTrigger} of the SDK's core layer. The action
 * registry obtains all action informations from the Relution system. To start the trigger, just
 * call the {@link #start} method. The start method will periodically update a list of iBeacons
 * each 10 seconds. In future versions actions will also be triggered when Relution tags will be
 * received..
 */
public class RelutionCampaignService {

    // Android
    private Context context;

    // Logging
    private static final String LOG_TAG = "RelutionTriggerService";

    // Registry
    IBeaconMessageActionMapper iBeaconMessageActionMapper;
    RelutionTagMessageActionMapperEmptyStub relutionTagMessageActionMapperStub;

    // Message processing graph
    private IBeaconMessageScanner scanner;
    private BeaconMessageActionTrigger trigger;

    public RelutionCampaignService(Context context,
                                   IBeaconMessageScanner scanner,
                                   Relution relution,
                                   int maximumQueueSize,
                                   long pollingTimeForCheckingRegistryAvailable,
                                   long pollingTimeForCheckingDelayedActionsInMs,
                                   long pollingTimeForCheckingLocksInMs,
                                   long aggregateDurationInMs) {
        this.context = context;
        this.scanner = scanner;
        //iBeaconMessageActionMapper = new IBeaconMessageActionMapperStub();
        iBeaconMessageActionMapper = new RelutionIBeaconMessageActionMapper(relution);
        relutionTagMessageActionMapperStub = new RelutionTagMessageActionMapperEmptyStub();
        RelutionActionRegistry actionRegistry = new RelutionActionRegistry(context,
                Tracer.getInstance(), iBeaconMessageActionMapper, relutionTagMessageActionMapperStub);
        trigger = new BeaconMessageActionTrigger(scanner, actionRegistry);

        // Configuration
        trigger.setMaximumQueueSize(maximumQueueSize);
        trigger.setPollingTimeForCheckingRegistryAvailable(pollingTimeForCheckingRegistryAvailable);
        trigger.setPollingTimeForCheckingDelayedActionsInMs(pollingTimeForCheckingDelayedActionsInMs);
        trigger.setPollingTimeForCheckingLocksInMs(pollingTimeForCheckingLocksInMs);
        trigger.setAggregateDurationInMs(aggregateDurationInMs);
    }

    public void start() {
        // Start the triggering mechanism.
        startTriggering();
    }

    private void startTriggering() {
        trigger.start();
    }

    public void addActionListener(BeaconActionListener listener) {
        trigger.addActionListener(listener);
    }

    // Debugging
    public void addDebugActionListener(BeaconActionDebugListener listener) {
        trigger.addDebugActionListener(listener);
    }

    public void stop() {
        stopTrigger();
    }

    private void stopTrigger() {
        this.trigger.stop();
    }
}
