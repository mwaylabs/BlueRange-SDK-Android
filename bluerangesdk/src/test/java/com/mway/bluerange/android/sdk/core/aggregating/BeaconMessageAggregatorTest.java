//
//  BeaconMessageAggregatorTest.java
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

package com.mway.bluerange.android.sdk.core.aggregating;

import com.mway.bluerange.android.sdk.core.logging.dummys.DummyTracer;
import com.mway.bluerange.android.sdk.helper.TestBlocker;
import com.mway.bluerange.android.sdk.core.aggregating.averaging.SimpleMovingAverageFilter;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class BeaconMessageAggregatorTest {

    private ITracer tracer;
    private BeaconMessageScannerSimulator simulator;
    private BeaconMessageAggregator aggregator;
    private TestBlocker testBlocker;
    private BeaconMessageStreamNodeReceiver receiver;

    @Before
    public void setUp() {
        tracer = new DummyTracer();
        simulator = new BeaconMessageScannerSimulator();

        aggregator = new BeaconMessageAggregator(tracer, simulator);
        aggregator.setAggregateDurationInMs(300);
        receiver = Mockito.mock(BeaconMessageStreamNodeReceiver.class);
        aggregator.addReceiver(receiver);
        aggregator.setAverageFilter(new SimpleMovingAverageFilter());

        testBlocker = new TestBlocker();
    }

    @Test
    public void testOneIncomingMessageShouldResultInOneAggregate() {
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -50);

        simulator.startScanning();
        testBlocker.blockTest(500);

        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(receiver, Mockito.times(1)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), argument.capture());
        Assert.assertEquals(-50, argument.getValue().getRssi());
    }

    @Test
    public void testTwoEqualMessagesShouldResultInOneAggregate() {
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -50);
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -60);

        simulator.startScanning();
        testBlocker.blockTest(500);

        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(receiver, Mockito.times(1)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), argument.capture());
    }

    @Test
    public void testTwoEqualMessagesShouldResultInTwoAggregatesIfAggregateDurationExpired() {
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -50);
        simulator.startScanning();
        simulator.resetSimulatedBeacons();
        testBlocker.blockTest(500);

        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -60);
        simulator.startScanning();
        simulator.resetSimulatedBeacons();
        testBlocker.blockTest(500);

        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(receiver, Mockito.times(2)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), argument.capture());
    }

    @Test
    public void testTwoDifferentMessagesShouldResultInTwoAggregatesEvenIfDurationNotExpired() {
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -50);
        simulator.simulateIBeaconWithRssi("c9407f30-f5f8-466e-aff9-25556b57fe6d", 2, 2, -60);

        simulator.startScanning();
        testBlocker.blockTest(500);

        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(receiver, Mockito.times(2)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), argument.capture());
    }

    @Test
    public void testRssiAverageInAggregate() {
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -50);
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -60);

        simulator.startScanning();
        testBlocker.blockTest(500);

        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(receiver, Mockito.times(1)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), argument.capture());
        Assert.assertEquals(-55, argument.getAllValues().get(0).getRssi());
    }

    @Test
    public void testSlidingWindowMode() {
        aggregator.setAggregationMode(BeaconMessageAggregator.AggregationMode.SLIDING_WINDOW);

        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -50);
        simulator.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 1, 1, -60);

        simulator.startScanning();
        testBlocker.blockTest(500);

        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(receiver, Mockito.times(2)).onReceivedMessage(
                Mockito.any(BeaconMessageStreamNode.class), argument.capture());
        Assert.assertEquals(-50, argument.getAllValues().get(0).getRssi());
        Assert.assertEquals(-55, argument.getAllValues().get(1).getRssi());
    }
}
