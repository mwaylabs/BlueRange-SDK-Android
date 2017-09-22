//
//  RelutionIoTServiceConfig.java
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

package com.mway.bluerange.android.sdk.services;

import com.mway.bluerange.android.sdk.core.triggering.BeaconMessageActionTrigger;
import com.mway.bluerange.android.sdk.core.triggering.rules.locking.BeaconActionLocker;
import com.mway.bluerange.android.sdk.services.configuration.RelutionScanConfigLoaderImpl;
import com.mway.bluerange.android.sdk.services.tags.RelutionTagInfoRegistryImpl;

public class RelutionIoTServiceConfig {

    // Login data
    static String baseUrl;
    static String organizationUuid;
    static String username;
    static String password;
    static boolean offlineMode = false;

    // Components activation
    static boolean loggingEnabled = false;
    static boolean campaignActionTriggerEnabled = true;
    static boolean heatmapGenerationEnabled = true;
    static boolean sendingAnalyticsDataEnabled = true;
    static boolean policyTriggerEnabled = true;
    static boolean relutionTagObservingEnabled = true;

    // Scanner
    static long scanPeriodInMillis = 500L;
    static long betweenScanPeriodInMillis = 500L;
    static long scannerMeshInactiveTimeoutInMs = 5000L;
    static long scannerBluetoothRestartDurationInMs = 100L;
    static long scannerBluetoothRestartPollingDurationInMs = 30000L;

    // Campaigns
    static int triggerMaximumQueueSize
            = BeaconMessageActionTrigger.DEFAULT_MAXIMUM_QUEUE_SIZE;
    static long triggerPollingTimeForCheckingRegistryAvailable
            = 5000L;
    static long triggerPollingTimeForCheckingDelayedActionsInMs
            = BeaconMessageActionTrigger.DEFAULT_POLLING_TIME_FOR_CHECKING_DELAYED_ACTIONS_IN_MS;
    static long triggerPollingTimeForCheckingLocksInMs
            = BeaconActionLocker.POLLING_TIME_FOR_CHECKING_LOCKS_IN_MS;
    static long triggerAggregateDurationInMs = 1000L;
    static long triggerWaitTimeBetweenUuidRegistrySynchronizationInMs
            = RelutionScanConfigLoaderImpl.DEFAULT_WAIT_TIME_BETWEEN_SYNCHRONIZATION_STEPS;

    // Heatmap Reporting
    static long reporterIntervalDurationInStatusReportsInMs = 3000L;
    static long reporterTimeBetweenStatusReportsInMs = 30000L;
    static long reporterPollingTimeToWaitForReceiverInMs = 60000L;

    // Relution Tag Registry
    public static long relutionTagRegistrySynchronizationTimeInMs
            = RelutionTagInfoRegistryImpl.DEFAULT_WAIT_TIME_BETWEEN_RELUTION_TAG_REGISTRY_SYNCHRONIZATIONS_IN_MS;

    // iBeacon calibration
}
