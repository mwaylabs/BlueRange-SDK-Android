//
//  BeaconTriggerTest.java
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

package com.mway.bluerange.android.sdk.services.trigger;

import com.mway.bluerange.android.sdk.core.distancing.AnalyticalDistanceEstimator;
import com.mway.bluerange.android.sdk.core.logging.dummys.DummyTracer;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.helper.TestBlocker;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeaconTriggerTest {

    private ITracer tracer;
    private AnalyticalDistanceEstimator distanceEstimator;
    private BeaconMessageScannerSimulator scanner;
    private BeaconTrigger trigger;
    private BeaconTrigger.BeaconTriggerObserver observerMock;
    private TestBlocker testBlocker;

    @Before
    public void setUp() {

        // Message processing
        tracer = new DummyTracer();
        distanceEstimator = new AnalyticalDistanceEstimator();
        scanner = new BeaconMessageScannerSimulator();
        trigger = new BeaconTrigger(tracer, scanner, distanceEstimator);
        testBlocker =  new TestBlocker();

        // Mode
        trigger.setMultiBeaconMode(false);

        // Beacons
        trigger.addRelutionTagTrigger(1L);

        // Reaction
        trigger.setReactionMode(BeaconTrigger.ReactionMode.PACKET);
        trigger.setReactionDurationInMs(0L);

        // Ranges
        trigger.setActivationDistance(2.0f);
        trigger.setInactivationDistance(3.0f);

        // Listener
        observerMock = Mockito.mock(BeaconTrigger
                .BeaconTriggerObserver.class);
        trigger.addObserver(observerMock);
    }

    @Test
    public void testTriggerShouldNotTriggerBeaconActiveIfOutsideRange() {
        // Scanner configuration and start
        simulateRelutionTag(1, 3.0f);
        scanner.startScanning();
        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(0);
    }

    @Test
    public void testTriggerShouldTriggerBeaconActiveIfInsideRange() {
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();
        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(1);
    }

    @Test
    public void testTriggerShouldTriggerBeaconActiveOnceIfInsideRange() {
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();
        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(1);
    }

    @Test
    public void testTriggerShouldNotTriggerBeaconInactiveIfOutsideRange() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Scanner configuration and start
        simulateRelutionTag(1, 2.5f);
        scanner.startScanning();

        testBlocker.blockTest(700L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(0);
    }

    @Test
    public void testTriggerShouldTriggerBeaconInactiveAfterActivationAndInactivityDurationElapsed() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        testBlocker.blockTest(700L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(1);
    }

    @Test
    public void testTriggerShouldNotTriggerBeaconInactiveIfNewMessagesComeIn() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        // Block once
        testBlocker.blockTest(350L);

        // New message
        scanner.resetSimulatedBeacons();
        simulateRelutionTag(1, 1.0f);
        scanner.restartSimulator();

        // Block twice
        testBlocker.blockTest(350L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(0);
    }

    @Test
    public void testTriggerShouldTriggerBeaconInactiveAfterNoMessagesCameInAndAfterInactivationDuration() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        // Block once
        testBlocker.blockTest(350L);

        // New message
        scanner.resetSimulatedBeacons();
        simulateRelutionTag(1, 1.0f);
        scanner.restartSimulator();

        // Block twice
        testBlocker.blockTest(600L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(1);
    }

    @Test
    public void testTriggerShouldTriggerBeaconInactiveEvenWhenNewMessagesComeInButOutsideInactivityRange() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        // Block once
        testBlocker.blockTest(350L);

        // New message with Distance outside inactivity range!
        scanner.resetSimulatedBeacons();
        simulateRelutionTag(1, 3.5f);
        scanner.restartSimulator();

        // Block twice
        testBlocker.blockTest(350L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(1);
    }

    @Test
    public void testTriggerShouldNotTriggerBeaconInactiveWhenNewMessagesComeInThatAreOutsideActivityRangeButInsideInactivityRange() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Scanner configuration and start
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        // Block once
        testBlocker.blockTest(350L);

        // New message with Distance outside activity range!
        scanner.resetSimulatedBeacons();
        simulateRelutionTag(1, 2.5f);
        scanner.restartSimulator();

        // Block twice
        testBlocker.blockTest(350L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(0);
    }

    @Test
    public void testTriggerShouldTriggerBeaconActiveAfterBeaconHasBecomeActiveAgain() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);

        // 1 active
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        // 2 inactive
        testBlocker.blockTest(700L);

        // 3 active again
        scanner.resetSimulatedBeacons();
        simulateRelutionTag(1, 1.0f);
        scanner.restartSimulator();

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(2);
        shouldHaveTriggeredBeaconInactive(1);
    }

    @Test
    public void testTriggerShouldTriggerTwoTimesBeaconActiveTwoTimesBeaconInactiveForStatePathActiveInactiveActiveInactive() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);

        // 1 active
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();

        // 2 inactive
        testBlocker.blockTest(700L);

        // 3 active again
        scanner.resetSimulatedBeacons();
        simulateRelutionTag(1, 1.0f);
        scanner.restartSimulator();

        // 2 inactive again
        testBlocker.blockTest(700L);

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(2);
        shouldHaveTriggeredBeaconInactive(2);
    }

    // Avoid premature activation changes from 'inactive' to 'active'.

    @Test
    public void testTriggerShouldTriggerDelayedIfOnlyOneMessageIsReceivedAndTriggerUsesReflectionTime() {
        // Configure trigger to not decide prematurely
        trigger.setReactionDurationInMs(150L);
        // Message inside activation range
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();
        // Validate before reflection time has finished.
        shouldHaveTriggeredBeaconActive(0);
        // Wait reflection time.
        testBlocker.blockTest(200L);
        // Validate after reflection time has finished.
        shouldHaveTriggeredBeaconActive(1);
    }

    @Test
    public void testTriggerShouldNotTriggerPrematurelyIfSubsequentMessagesDisqualifyFirstOne() {
        // Configure trigger to not decide prematurely
        trigger.setReactionDurationInMs(150L);
        // Message inside activation range.
        simulateRelutionTag(1, 1.0f);
        // Message outside activation range. Since the
        // RSSI values are averaged and not the distance values,
        // high distance values will not influence the average so much,
        // which is good, because a high distance value does not always
        // have to be a real high distance, but may be computed due
        // to moving objects etc.
        simulateRelutionTag(1, 10f);
        scanner.startScanning();
        // Wait reflection time.
        testBlocker.blockTest(200L);
        // Validation
        shouldHaveTriggeredBeaconActive(0);
    }

    @Test
    public void testTriggerShouldTriggerOnlyOnceInsideReflectionTimeWindowIPacketReflectionModeIsUsed() {
        trigger.setReactionMode(BeaconTrigger.ReactionMode.PACKET);
        // Configure trigger to not trigger prematurely.
        trigger.setReactionDurationInMs(150L);
        // Simulate three Message inside activation range.
        simulateRelutionTag(1, 1.0f);
        simulateRelutionTag(1, 1.0f);
        simulateRelutionTag(1, 1.0f);
        scanner.startScanning();
        // Wait reflection time.
        testBlocker.blockTest(200L);
        // Validation
        shouldHaveTriggeredBeaconActive(1);
    }

    // Filtering
    @Test
    public void testTriggerShouldOnlyScanRelutionTagMessages() {
        // Scanner configuration and start
        simulateJoinMe(1, 1.0f);
        scanner.startScanning();
        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(0);
    }

    @Test
    public void testTriggerShouldNotTriggerBeaconActiveIfRelutionTagIsWrong() {
        // Scanner configuration and start
        simulateRelutionTag(2, 1.0f);
        scanner.startScanning();
        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(0);
    }

    // Multiple triggers

    // Beacon1 (outside), Beacon 2 (outside) -> 1 Trigger
    @Test
    public void testTriggerShouldNotTriggerIfOutsideBothBeacons() {
        // Add second "beacon"
        trigger.addRelutionTagTrigger(2L);

        // outside inactivity range
        simulateRelutionTag(1, 10.0f);
        // inside activity range
        simulateRelutionTag(2, 10.0f);
        scanner.startScanning();

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(0);
    }

    // Beacon1 (inside inactivity range), Beacon 2 (inside activity range) -> 1 Trigger
    @Test
    public void testTriggerShouldTriggerOnceIfInsideInactivityRangeOfBeacon1AndInsideActivityRangeOfBeacon2() {
        // Add second "beacon"
        trigger.addRelutionTagTrigger(2L);

        // inside activity range
        simulateRelutionTag(1, 1.0f);
        // inside inactivity range
        simulateRelutionTag(2, 2.5f);
        scanner.startScanning();

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(new long[] {1}, 1);
    }

    // Beacon1 (outside inactivity range), Beacon 2 (inside) -> 1 Trigger
    @Test
    public void testTriggerShouldTriggerOnceIfOutsideInactivityRangeOfBeacon1AndInsideBeacon2() {
        // Add second "beacon"
        trigger.addRelutionTagTrigger(2L);

        // outside inactivity range
        simulateRelutionTag(1, 10.0f);
        // inside activity range
        simulateRelutionTag(2, 1.0f);
        scanner.startScanning();

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(new long[] {2}, 1);
    }

    // Beacon1 (inside activation range), Beacon 2 (inside activation range)
    // -> 1 Trigger! Only one beacon should be active at a time. Another
    // beacon should only be triggered, when we have left the current active beacon.
    @Test
    public void testTriggerShouldTriggerOnceIfInTwoActivityRegion() {
        // Add second "beacon"
        trigger.addRelutionTagTrigger(2L);

        // Both inside
        simulateRelutionTag(1, 1.0f);
        simulateRelutionTag(2, 1.0f);
        scanner.startScanning();

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconActive(new long[] {1}, 1);
    }

    // Beacon 1 active, Beacon 2 inactive -> Beacon 1 should be triggered
    // Then: Beacon 2 active, Beacon 1 inactive -> Beacon 2 should be triggered
    @Test
    public void testTriggerShouldTriggerOnlyCurrentlyActiveBeacon() {
        // Configure trigger
        trigger.setInactivationDurationInMs(500L);
        // Add second "beacon"
        trigger.addRelutionTagTrigger(2L);

        // Beacon 1 active
        simulateRelutionTag(1, 1.0f);
        // Beacon 2 inactive
        simulateRelutionTag(2, 2.5f);
        scanner.startScanning();

        // Wait until inactivation
        testBlocker.blockTest(trigger.getInactivationDurationInMs() + 50);
        waitTriggerReactionTime();

        scanner.resetSimulatedBeacons();
        // Beacon 1 inactive
        simulateRelutionTag(1, 2.5f);
        // Beacon 2 active
        simulateRelutionTag(2, 1.0f);
        scanner.restartSimulator();

        // Validation
        waitTriggerReactionTime();
        shouldHaveTriggeredBeaconInactive(new long[]{1}, 1);
        shouldHaveTriggeredBeaconActive(new long[]{1,2}, 2);
    }

    private void simulateRelutionTag(int tag, float distanceInMeters) {
        scanner.simulateRelutionTagsV1WithRssi(new long[]{tag},
                (int) distanceEstimator.distanceToRssiWithA(distanceInMeters, BeaconTrigger
                        .TX_POWER));
    }

    private void simulateJoinMe(int nodeId, float distanceInMeters) {
        scanner.simulateJoinMe(nodeId,
                (int) distanceEstimator.distanceToRssiWithA(distanceInMeters, BeaconTrigger
                        .TX_POWER));
    }

    private void waitTriggerReactionTime() {
        testBlocker.blockTest(100);
    }

    private void shouldHaveTriggeredBeaconActive(int times) {
        shouldHaveTriggeredBeaconActive(new long[]{1L}, times);
    }

    private void shouldHaveTriggeredBeaconActive(long[] tags, int times) {
        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(observerMock, Mockito.times(times)).onBeaconActive(argument.capture());
        if (times > 0) {
            List<BeaconMessage> messages = argument.getAllValues();
            checkIfMessagesContainTags(messages, tags);
        }
    }

    private void checkIfMessagesContainTags(List<BeaconMessage> messages, long[] tags) {
        Set<Long> actualTags = new HashSet<>();
        for (BeaconMessage message : messages) {
            RelutionTagMessageV1 relutionTagMessage = (RelutionTagMessageV1) message;
            actualTags.addAll(relutionTagMessage.getTags());
        }
        Set<Long> expectedTags = new HashSet<>(convertLongs(tags));
        Assert.assertEquals(expectedTags, actualTags);
    }

    private void shouldHaveTriggeredBeaconInactive(int times) {
        shouldHaveTriggeredBeaconInactive(new long[] {1L}, times);
    }

    private void shouldHaveTriggeredBeaconInactive(long[] tags, int times) {
        ArgumentCaptor<BeaconMessage> argument = ArgumentCaptor.forClass(BeaconMessage.class);
        Mockito.verify(observerMock, Mockito.times(times)).onBeaconInactive(argument
                .capture());
        if (times > 0) {
            List<BeaconMessage> messages = argument.getAllValues();
            checkIfMessagesContainTags(messages, tags);
        }
    }

    private static List<Long> convertLongs(long[] longs) {
        List<Long> longList = new ArrayList<>(longs.length);
        for (long n : longs) {
            longList.add(n);
        }
        return longList;
    }
}
