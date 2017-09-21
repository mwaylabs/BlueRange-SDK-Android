//
//  RelutionScanConfigLoaderImpl.java
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

package com.mway.bluerange.android.sdk.services.configuration;

import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerConfig;
import com.mway.bluerange.android.sdk.core.scanning.IBeaconMessageScanner;
import com.mway.bluerange.android.sdk.services.relution.Relution;
import com.mway.bluerange.android.sdk.services.relution.model.AdvertisingMessagesConfiguration;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RelutionScanConfigLoaderImpl implements RelutionScanConfigLoader {

    // Logger
    private static final String LOG_TAG = RelutionScanConfigLoaderImpl.class.getSimpleName();
    private ITracer tracer = Tracer.getInstance();

    // Scanner
    private IBeaconMessageScanner scanner;

    public static final long DEFAULT_WAIT_TIME_BETWEEN_SYNCHRONIZATION_STEPS
            = 10000l;
    private long waitTimeBetweenUuidRegistrySynchronizationInMs
            = DEFAULT_WAIT_TIME_BETWEEN_SYNCHRONIZATION_STEPS;

    private Thread scannerConfigSynchronizationThread;

    private Relution relution;

    public RelutionScanConfigLoaderImpl
            (Relution relution,
             IBeaconMessageScanner scanner,
            long waitTimeBetweenUuidRegistrySynchronizationInMs) {
        this.relution = relution;
        this.scanner = scanner;
        this.waitTimeBetweenUuidRegistrySynchronizationInMs
                = waitTimeBetweenUuidRegistrySynchronizationInMs;
    }

    public void start() {
        // 1. Obtain the list of UUIDs in background and update it periodically.
        startScannerConfigSynchronization();
    }

    private void startScannerConfigSynchronization() {
        this.scannerConfigSynchronizationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Periodically update the scanner's configuration
                    while (true) {
                        // 1. Synchronize scan configuration
                        trySynchronizeScanConfiguration();
                        // 2. Wait a while
                        waitUntilNextSynchronization();
                    }
                } catch (InterruptedException e) {
                    // An interrupt stops this thread.
                }
            }
        });
        this.scannerConfigSynchronizationThread.setName("RelutionCampaignService-UUID-Synchronization");
        this.scannerConfigSynchronizationThread.start();
    }

    private void trySynchronizeScanConfiguration() {
        try {
            synchronizeScanConfiguration();
        } catch (Exception e) {
            tracer.logWarning(LOG_TAG, "A problem ocurred while " +
                    "synchronizing scan configuration. " + e.getMessage());
        }
    }

    private void waitUntilNextSynchronization() throws InterruptedException {
        Thread.sleep(waitTimeBetweenUuidRegistrySynchronizationInMs);
    }

    public void synchronizeScanConfiguration() throws Exception {
        synchronizeScanConfigWithRelution();
    }

    private void synchronizeScanConfigWithRelution() throws Exception {
        AdvertisingMessagesConfiguration advertisingMessagesConfiguration
                = relution.getAdvertisingMessagesConfiguration();
        parseJSONArrayAndChangeScanConfig(advertisingMessagesConfiguration);
    }

    private void parseJSONArrayAndChangeScanConfig(AdvertisingMessagesConfiguration configuration) {
        try {
            JSONArray jsonArray = configuration.getJsonArray();
            JSONObject result = jsonArray.getJSONObject(0);

            String relultionTagV2Namespace = result.getString("relutionTagV2Namespace");
            configRelutionTagMessages(relultionTagV2Namespace);

            JSONArray iBeaconUuids = result.getJSONArray("iBeaconUuids");
            configIBeaconUuids(iBeaconUuids);

            JSONArray eddystoneNamespaces = result.getJSONArray("eddystoneNamespaces");
            configEddystoneNamespaces(eddystoneNamespaces);

        } catch (JSONException e) {
            tracer.logWarning(LOG_TAG, "Error while parsing scan configuration. " + e.getMessage());
        }
    }

    private void configRelutionTagMessages(String relutionTagV2Namespace) {
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanRelutionTags(relutionTagV2Namespace);
    }

    private void configIBeaconUuids(JSONArray iBeaconUuids) {
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < iBeaconUuids.length(); i++) {
            try {
                String iBeaconUuid = iBeaconUuids.getString(i);
                uuids.add(iBeaconUuid);
            } catch (Exception e) {
                tracer.logWarning(LOG_TAG, "Error while parsing json array of iBeacon UUIDs. "
                        + e.getMessage());
            }
        }
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanIBeaconUuids(uuids);
    }

    private void configEddystoneNamespaces(JSONArray eddystoneNamespaces) {
        List<String> namespaces = new ArrayList<>();
        for (int i = 0; i < eddystoneNamespaces.length(); i++) {
            try {
                String namespaceUid = eddystoneNamespaces.getString(i);
                namespaces.add(namespaceUid);
            } catch (Exception e) {
                tracer.logWarning(LOG_TAG, "Error while parsing json array of Eddystone namespaces. "
                        + e.getMessage());
            }
        }
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanEddystoneUid(namespaces);
    }

    @Override
    public void stop() {
        this.scannerConfigSynchronizationThread.interrupt();
    }
}
