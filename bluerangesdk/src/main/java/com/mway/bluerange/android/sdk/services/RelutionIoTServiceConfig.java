//
//  RelutionIoTServiceConfig.java
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
