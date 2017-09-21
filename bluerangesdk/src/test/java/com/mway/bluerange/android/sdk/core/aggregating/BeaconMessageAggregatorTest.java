//
//  BeaconMessageAggregatorTest.java
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
