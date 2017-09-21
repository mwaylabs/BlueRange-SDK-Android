//
//  RelutionCampaignService.java
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
