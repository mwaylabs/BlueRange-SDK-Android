//
//  BeaconMessageScannerConfigTest.java
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

package com.mway.bluerange.android.sdk.core.scanning;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class BeaconMessageScannerConfigTest {
    @Test
    public void shouldNotRestartScannerOnConfigChangeIfScannerHasNotBeenStarted() {
        // 1. Configuration before scanning
        BeaconMessageScannerSimulator scanner = new BeaconMessageScannerSimulator();
        BeaconMessageStreamNodeReceiver mockReceiver = Mockito.mock(BeaconMessageStreamNodeReceiver.class);
        scanner.addReceiver(mockReceiver);

        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanIBeacon("b2407f30-f5f8-466e-aff9-25556b57fe6d");
        scanner.simulateIBeacon("b2407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1);

        Mockito.verify(mockReceiver, Mockito.times(0)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), Mockito.any(BeaconMessage.class));
    }

    @Test
    public void shouldRestartScannerOnConfigChangeIfScannerIsRunning() {
        // 1. Configuration before scanning
        BeaconMessageScannerSimulator scanner = new BeaconMessageScannerSimulator();
        BeaconMessageStreamNodeReceiver mockReceiver = Mockito.mock(BeaconMessageStreamNodeReceiver.class);
        scanner.addReceiver(mockReceiver);

        final String uuid1 = "b2407f30-f5f8-466e-aff9-25556b57fe6d";
        final String uuid2 = "c2407f30-f5f8-466e-aff9-25556b57fe6d";
        BeaconMessageScannerConfig config = scanner.getConfig();
        scanner.simulateIBeacon(uuid1, 1, 1);
        config.scanIBeacon(uuid1);
        scanner.startScanning();
        scanner.resetSimulatedBeacons();
        scanner.simulateIBeacon(uuid2, 1, 1);
        config.scanIBeacon(uuid2);

        ArgumentCaptor<IBeaconMessage> messageArgument = ArgumentCaptor.forClass(IBeaconMessage.class);
        Mockito.verify(mockReceiver, Mockito.times(2)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), messageArgument.capture());
        List<IBeaconMessage> messages = messageArgument.getAllValues();
        Assert.assertEquals(UUID.fromString(uuid1), messages.get(0).getUUID());
        Assert.assertEquals(UUID.fromString(uuid2), messages.get(1).getUUID());
    }
}
