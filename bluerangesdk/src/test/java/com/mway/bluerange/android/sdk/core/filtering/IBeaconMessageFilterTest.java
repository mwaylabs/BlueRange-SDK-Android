//
//  IBeaconMessageFilterTest.java
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

package com.mway.bluerange.android.sdk.core.filtering;

import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class IBeaconMessageFilterTest {

    private BeaconMessageScannerSimulator scanner;
    private IBeaconMessageFilter iIBeaconMessageFilter;
    private BeaconMessageStreamNode receiverNode;

    @Before
    public void setUp() {
        // Initialize
        this.scanner = new BeaconMessageScannerSimulator();
        this.iIBeaconMessageFilter = new IBeaconMessageFilter(scanner);
        // Configure receiver
        this.receiverNode = Mockito.mock(BeaconMessageStreamNode.class);
        this.iIBeaconMessageFilter.addReceiver(receiverNode);
    }

    @Test
    public void testIBeaconPassingThroughFilter() {
        // Configure and start sender.
        scanner.simulateIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        scanner.startScanning();
        // Verify that message was passed.
        Mockito.verify(receiverNode, Mockito.times(1)).onReceivedMessage(
                Mockito.any(IBeaconMessageFilter.class), Mockito.any(IBeaconMessage.class));
    }

    @Test
    public void testNonIBeaconDoesNotPassFilter() {
        // Configure and start sender.
        scanner.simulateRelutionTagsV1(new long[]{1, 2});
        scanner.startScanning();
        // Verify that message was not passed
        Mockito.verify(receiverNode, Mockito.times(0)).onReceivedMessage(
                Mockito.any(IBeaconMessageFilter.class), Mockito.any(IBeaconMessage.class));
    }
}
