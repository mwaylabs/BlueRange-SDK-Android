//
//  RelutionHeatmapService.java
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

package com.mway.bluerange.android.sdk.services.analytics;

import android.content.Context;

import com.mway.bluerange.android.sdk.core.logging.BeaconMessageLogger;
import com.mway.bluerange.android.sdk.core.reporting.BeaconMessageReport;
import com.mway.bluerange.android.sdk.core.reporting.BeaconMessageReporter;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerConfig;
import com.mway.bluerange.android.sdk.core.reporting.BeaconMessageReportSender;
import com.mway.bluerange.android.sdk.core.scanning.IBeaconMessageScanner;
import com.mway.bluerange.android.sdk.services.relution.Relution;

import org.json.JSONObject;

/**
 * This class periodically sends status reports to Relution. The reports contain the received
 * signal strength of all beacons for all time intervals, since this service has been started.
 * Each time interval has a maximum duration of 3 seconds. A report will be sent, whenever the
 * 50th Join me message was received or no beacon has been detected in the last 5 seconds.
 */
public class RelutionHeatmapService {

    private static final String LOG_TAG = RelutionHeatmapService.class.getSimpleName();

    private Context context;
    private IBeaconMessageScanner scanner;
    private BeaconMessageLogger logger;
    private BeaconMessageReporter reporter;
    private BeaconMessageReportSender sender;

    private Relution relution;

    // Configuration
    private long intervalDurationInMs;
    private long timeBetweenReportsInMs;
    private long pollingTimeWaitForReceiverAvailableInMs;

    public RelutionHeatmapService(Context context,
                                  IBeaconMessageScanner scanner,
                                  Relution relution,
                                  long intervalDurationInMs,
                                  long timeBetweenReportsInMs,
                                  long pollingTimeWaitForReceiverAvailableInMs) {
        this.context = context.getApplicationContext();
        this.scanner = scanner;
        this.relution = relution;
        this.intervalDurationInMs = intervalDurationInMs;
        this.timeBetweenReportsInMs = timeBetweenReportsInMs;
        this.pollingTimeWaitForReceiverAvailableInMs = pollingTimeWaitForReceiverAvailableInMs;

        /*this.sender = new BeaconMessageReportSender() {
            @Override
            public boolean receiverAvailable() {
                return true;
            }

            @Override
            public void sendReport(BeaconMessageReport report) throws SendReportException {
                RelutionHeatmapReport relutionHeatmapReport = (RelutionHeatmapReport) report;
                Log.d(LOG_TAG, "Sending report" + relutionHeatmapReport.getJsonObject().toString());
            }
        };*/
        this.sender = new BeaconMessageReportSender() {
            @Override
            public boolean receiverAvailable() {
                return RelutionHeatmapService.this.relution.isServerAvailable();
            }

            @Override
            public void sendReport(BeaconMessageReport report)
                    throws SendReportException, UnresolvableSendReportException {
                if (report instanceof RelutionHeatmapReport) {
                    RelutionHeatmapReport relutionHeatmapReport = (RelutionHeatmapReport)report;
                    JSONObject jsonReport = relutionHeatmapReport.getJsonObject();
                    try {
                        RelutionHeatmapService.this.relution.sendAnalyticsReport(jsonReport);
                    } catch (Throwable t) {
                        throw new SendReportException();
                    }
                }
            }
        };
    }

    public void start() {
        // Configure BeaconMessageScanner
        configureBeaconScanner();
        // Configure BeaconMessageLogger
        logger = new BeaconMessageLogger(
                scanner, context);
        // Configure BeaconMessageReporter
        RelutionHeatmapReportBuilder reportBuilder = new RelutionHeatmapReportBuilder(
                relution.getOrganizationUuid());
        reportBuilder.setIntervalDurationInMs(
                this.intervalDurationInMs);
        reporter = new BeaconMessageReporter(
                logger, reportBuilder, this.sender);
        reporter.setTimeBetweenReportsInMs(
                this.timeBetweenReportsInMs);
        reporter.setPollingTimeWaitForReceiverAvailableInMs(
                this.pollingTimeWaitForReceiverAvailableInMs);
        // Start reporting
        reporter.startReporting();
    }

    private void configureBeaconScanner() {
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanJoinMeMessage();
    }

    public void stop() {
        stopReporter();
        stopLogger();
    }

    private void stopReporter() {
        reporter.stopReporting();
    }

    private void stopLogger() {
        // Nothing has to be stopped.
    }
}
