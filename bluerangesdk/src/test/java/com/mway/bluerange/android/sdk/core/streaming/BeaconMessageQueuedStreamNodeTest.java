//
//  BeaconMessageQueuedStreamNodeTest.java
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

package com.mway.bluerange.android.sdk.core.streaming;

import com.mway.bluerange.android.sdk.helper.TestBlocker;
import com.mway.bluerange.android.sdk.helper.TestFailure;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import java.util.UUID;

/**
 *
 */
public class BeaconMessageQueuedStreamNodeTest {

    private BeaconMessageScannerSimulator scanner;
    private BeaconMessageQueuedStreamNode queue;

    @Before
    public void setUp() {
        // Create scanner -> queue graph
        this.scanner = new BeaconMessageScannerSimulator();
        this.queue = new BeaconMessageQueuedStreamNode(scanner);
    }

    @Test
    public void testShouldReceiveOneMessageWhenReceivedByQueue() throws InterruptedException {
        // Define iBeacon
        IBeacon iBeacon = new IBeacon(UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 1, 1);
        // Prepare scanner
        scanner.simulateIBeacon(iBeacon);
        // Simulate scanner
        scanner.startScanning();
        // Pull messages
        IBeaconMessage actualMessage = (IBeaconMessage)queue.pullBeaconMessage();
        // Verify
        Assert.assertEquals(iBeacon, actualMessage.getIBeacon());
    }

    @Test
    public void testShouldReceiveTwoMessagesWhenTwoMessagesReceiveQueue() throws InterruptedException {
        // Define iBeacon
        IBeacon iBeacon1 = new IBeacon(UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 1, 1);
        IBeacon iBeacon2 = new IBeacon(UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 1, 1);
        // Prepare scanner
        scanner.simulateIBeacon(iBeacon1);
        scanner.simulateIBeacon(iBeacon2);
        // Simulate scanner
        scanner.startScanning();
        // Pull messages
        IBeaconMessage actualMessage1 = (IBeaconMessage)queue.pullBeaconMessage();
        IBeaconMessage actualMessage2 = (IBeaconMessage)queue.pullBeaconMessage();
        // Verify
        Assert.assertEquals(iBeacon1, actualMessage1.getIBeacon());
        Assert.assertEquals(iBeacon2, actualMessage2.getIBeacon());
    }

    @Test
    public void testShouldNotReceiveTwoMessagesIfMaximumMessagesIsOne() throws InterruptedException {
        final TestFailure failure = new TestFailure();
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.setMaximumSize(1);
                // Define iBeacon
                IBeacon iBeacon1 = new IBeacon(UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 1, 1);
                IBeacon iBeacon2 = new IBeacon(UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 1, 1);
                // Prepare scanner
                scanner.simulateIBeacon(iBeacon1);
                scanner.simulateIBeacon(iBeacon2);
                // Simulate scanner
                scanner.startScanning();
                // Pull messages
                try {
                    IBeaconMessage actualMessage1 = (IBeaconMessage)queue.pullBeaconMessage();
                    IBeaconMessage actualMessage2 = (IBeaconMessage)queue.pullBeaconMessage();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // We assume that we should not reach this code line
                failure.setFailed(true);
            }
        }).start();

        // Wait a second
        TestBlocker testBlocker = new TestBlocker();
        testBlocker.blockTest(100);

        Assert.assertFalse(failure.isFailed());
    }
}
