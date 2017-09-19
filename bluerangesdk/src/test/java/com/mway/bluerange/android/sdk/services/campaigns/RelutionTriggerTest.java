//
//  RelutionTriggerTest.java
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

package com.mway.bluerange.android.sdk.services.campaigns;

import com.mway.bluerange.android.sdk.core.logging.dummys.DummyTracer;
import com.mway.bluerange.android.sdk.core.triggering.BeaconAction;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionListener;
import com.mway.bluerange.android.sdk.core.triggering.BeaconMessageActionTrigger;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionRegistry;
import com.mway.bluerange.android.sdk.helper.TestBlocker;
import com.mway.bluerange.android.sdk.core.aggregating.BeaconMessageAggregator;
import com.mway.bluerange.android.sdk.core.distancing.AnalyticalDistanceEstimator;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.IBeaconMessageActionMapperStub;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.content.RelutionContentAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag.RelutionTagVisit;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag.RelutionTagAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.registry.RelutionTagMessageActionMapperStub;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;

/**
 *
 */
public class RelutionTriggerTest {

    private final TestBlocker testBlocker = new TestBlocker();
    private BeaconMessageScannerSimulator scanner;
    private IBeaconMessageActionMapperStub iBeaconMessageActionMapper;
    private RelutionTagMessageActionMapperStub relutionTagMessageActionMapper;
    private BeaconMessageActionTrigger trigger;
    private BeaconMessageAggregator aggregator;
    private BeaconActionListener mockListener;

    @Before
    public void setUp() {
        ITracer tracer = new DummyTracer();
        scanner = new BeaconMessageScannerSimulator();
        iBeaconMessageActionMapper = new IBeaconMessageActionMapperStub();
        relutionTagMessageActionMapper = new RelutionTagMessageActionMapperStub();
        RelutionActionRegistry actionRegistry = new RelutionActionRegistry(null,
                tracer, iBeaconMessageActionMapper, relutionTagMessageActionMapper);
        this.trigger = new BeaconMessageActionTrigger(tracer, scanner,
                actionRegistry, new AnalyticalDistanceEstimator());
        this.aggregator = trigger.getAggregator();
        this.aggregator.setAggregateDurationInMs(0l);
        mockListener = Mockito.mock(BeaconActionListener.class);
        trigger.addActionListener(mockListener);
    }

    @Test
    public void testLeaveMessageOutIfMessageIsNotSupported() {
        // Malformed iBeacon
        scanner.simulateIBeacon("ffffffff-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Valid iBeacon
        scanner.simulateIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test
        testBlocker.blockTest();
        // Validate
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(argument.capture());
        Assert.assertEquals("testContent", argument.getValue().getContent());
    }

    @Test
    public void testContentActionListenerNotCalledWhenNoMessageArrives() {
        // Simulate no messages
        // ...
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test
        testBlocker.blockTest();
        // Validate
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(Mockito.any(RelutionContentAction.class));
    }

    @Test
    public void testContentActionListenerCalledWhenBeaconMessageArrives() {
        // Simulate iBeacon messages
        scanner.simulateIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test
        testBlocker.blockTest();
        // Validate
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(argument.capture());
        Assert.assertEquals("testContent", argument.getValue().getContent());
    }

    @Test
    public void testBeaconActionExpires() throws JSONException {
        scanner.simulateIBeacon("d9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        trigger.start();
        scanner.startScanning();
        testBlocker.blockTest();
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(Mockito.any(BeaconAction.class));
    }

    @Test
    public void testBeaconActionDelayed() throws JSONException {
        scanner.simulateIBeacon("e9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();

        // Wait a small amount of time. Action should not have been executed.
        testBlocker.blockTest(1000);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(Mockito.any(BeaconAction.class));

        // Wait a little bit longer. Now the the action should have been executed.
        testBlocker.blockTest(2000);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(Mockito.any(BeaconAction.class));
    }

    @Test
    public void testUseDefaultParametersWhenParameterNotExists() {
        // Simulate iBeacon messages
        scanner.simulateIBeacon("f9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test
        testBlocker.blockTest();
        // Validate
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(argument.capture());
    }

    @Test
    public void testLockingActionLocksIncomingAction() {
        // Simulate iBeacon messages corresponding with actions that have
        // a large repeatEvery parameter.
        scanner.simulateIBeacon("09407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        scanner.simulateIBeacon("09407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test 1000 milliseconds (inside locking time range (5000 milliseconds lock range))
        testBlocker.blockTest(1000);
        // Validate that action was executed only once
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(argument.capture());
    }

    @Test
    public void testLockingActionDoesNotLockAfterLockRelease() {
        // Define an iBeacon corresponding with a locking action.
        scanner.simulateIBeacon("39407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test 1500 milliseconds (outside locking time range (1000 milliseconds lock range))
        testBlocker.blockTest(2000);
        // Send the same message once again
        scanner.resetSimulatedBeacons();
        scanner.simulateIBeacon("39407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        scanner.startScanning();
        // Wait a short time
        testBlocker.blockTest(500);
        // Verify that the action was executed twice!
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(2)).onActionTriggered(argument.capture());
    }

    @Test
    public void testLockingShouldNotLockOtherActions() {
        // Simulate iBeacon corresponding with a locking action (with high lock duration).
        scanner.simulateIBeacon("09407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Simulate an iBeacon message corresponding with a different action (default content action).
        // This message corresponds with another action id!
        scanner.simulateIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test 1000 milliseconds
        testBlocker.blockTest(1000);
        // Verify that the action was executed twice since the second action should not be blocked
        // due to their different action identifiers!
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(2)).onActionTriggered(argument.capture());
    }

    @Test
    public void testActionShouldNotBeExecutedIfMessageWasReceivedOutsideRange() {
        // Simulate iBeacon associated with content action and low distance threshold.
        int rssi = (int)new AnalyticalDistanceEstimator().distanceToRssi(10);
        scanner.simulateIBeaconWithRssi("49407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, rssi);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that action was not executed.
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(argument.capture());
    }

    @Test
    public void testActionShouldBeExecutedIfMessageWasReceivedWithinRange() {
        // Simulate iBeacon associated with content action and low distance threshold.
        int rssi = (int)new AnalyticalDistanceEstimator().distanceToRssi(1);
        scanner.simulateIBeaconWithRssi("49407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, rssi);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that action was not executed.
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(argument.capture());
    }

    @Test
    public void testActionShouldBeExecutedIfNoRangeIsSpecified() {
        // Simulate iBeacon associated with content action and low distance threshold.
        int rssi = (int)new AnalyticalDistanceEstimator().distanceToRssi(10);
        scanner.simulateIBeaconWithRssi("59407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, rssi);
        // Start trigger and scanner
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that action was not executed.
        ArgumentCaptor<RelutionContentAction> argument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(argument.capture());
    }

    @Test
    public void testVisitedOneTag() {
        // Simulate iBeacon that is associated with a BeaconTagAction.
        scanner.simulateIBeaconWithRssi("69407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        ArgumentCaptor<RelutionTagVisit> tagArgument = ArgumentCaptor.forClass(RelutionTagVisit.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(actionArgument.capture());
        RelutionTagVisit actualTag  = actionArgument.getValue().getTag();
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Checkout", -45), actualTag);
    }

    @Test
    public void testVisitedTwoTags() {
        // Simulate iBeacon that is associated with a BeaconTagAction.
        scanner.simulateIBeaconWithRssi("79407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -45);
        scanner.simulateIBeaconWithRssi("69407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        ArgumentCaptor<RelutionTagVisit> tagArgument = ArgumentCaptor.forClass(RelutionTagVisit.class);
        Mockito.verify(mockListener, Mockito.times(2)).onActionTriggered(actionArgument.capture());
        List<RelutionTagAction> actions  = actionArgument.getAllValues();
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Foyer", -45), actions.get(0).getTag());
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Checkout", -45), actions.get(1).getTag());
    }

    @Test
    public void testActionShouldNotBeExecutedIfCampaignIsExpired() {
        // Simulate iBeacon with expired Campaign
        scanner.simulateIBeacon("89407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was not executed.
        ArgumentCaptor<RelutionContentAction> actionArgument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(actionArgument.capture());
    }

    @Test
    public void testActionShouldNotBeExecutedIfCampaignIsNotActiveYet() {
        // Simulate iBeacon with Campaign that is not active yet.
        scanner.simulateIBeacon("99407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was not executed.
        ArgumentCaptor<RelutionContentAction> actionArgument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(actionArgument.capture());
    }

    @Test
    public void testTwoActionsShouldBeExecutedEvenWhenTheyAreInDifferentCampaigns() {
        // Simulate iBeacon associated with two actions being defined for different campaigns.
        scanner.simulateIBeacon("91407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was not executed.
        ArgumentCaptor<RelutionContentAction> actionArgument = ArgumentCaptor.forClass(RelutionContentAction.class);
        Mockito.verify(mockListener, Mockito.times(2)).onActionTriggered(actionArgument.capture());
    }

    @Test
    public void testRelutionTagMessageWithOneTag() {
        // Simulate Relution tag message
        scanner.simulateRelutionTagsV1WithRssi(new long[]{1}, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        ArgumentCaptor<RelutionTagVisit> tagArgument = ArgumentCaptor.forClass(RelutionTagVisit.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(actionArgument.capture());
        RelutionTagVisit actualTag  = actionArgument.getValue().getTag();
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Bananas", -45), actualTag);
    }

    @Test
    public void testRelutionTagMessageWithTwoTags() {
        // Simulate Relution tag message with two tags
        scanner.simulateRelutionTagsV1WithRssi(new long[]{1, 2}, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        Mockito.verify(mockListener, Mockito.times(2)).onActionTriggered(actionArgument.capture());
        List<RelutionTagAction> actualTags  = actionArgument.getAllValues();
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Bananas", -45), actualTags.get(0).getTag());
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Apples", -45), actualTags.get(1).getTag());
    }

    @Test
    public void testTwoRelutionTagMessagesWithOneTag() {
        // Simulate Relution tag message with two tags
        scanner.simulateRelutionTagsV1WithRssi(new long[]{1}, -45);
        scanner.simulateRelutionTagsV1WithRssi(new long[]{2}, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        Mockito.verify(mockListener, Mockito.times(2)).onActionTriggered(actionArgument.capture());
        List<RelutionTagAction> actualTags  = actionArgument.getAllValues();
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Bananas", -45), actualTags.get(0).getTag());
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Apples", -45), actualTags.get(1).getTag());
    }

    @Test
    public void testShouldSkipCorruptRelutionTagMessage() {
        // Simulate Relution tag message with two tags
        scanner.simulateRelutionTagsV1WithRssi(new long[]{-1}, -45);
        // Unknown tag!
        scanner.simulateRelutionTagsV1WithRssi(new long[]{1}, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        Mockito.verify(mockListener, Mockito.times(1)).onActionTriggered(actionArgument.capture());
        List<RelutionTagAction> actualTags  = actionArgument.getAllValues();
        Assert.assertEquals(new RelutionTagVisit(new Date(0), "Bananas", -45), actualTags.get(0).getTag());
    }

    @Test
    public void testShouldSkipCompleteMessageIfOneTagIsCorrupt() {
        // Simulate Relution tag message with two tags. The last one is corrupt.
        scanner.simulateRelutionTagsV1WithRssi(new long[]{1, -1}, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(actionArgument.capture());
    }

    @Test
    public void testShouldSkipMessageIfRegistryNotAvailable() {
        // Set unavailable
        relutionTagMessageActionMapper.setUnexpectedUnavailable(true);
        // Simulate Relution tag message with one tag.
        scanner.simulateRelutionTagsV1WithRssi(new long[]{1}, -45);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(actionArgument.capture());
    }

    @Test
    public void testShouldSkipMessageIfResultIsCorrupt() {
        iBeaconMessageActionMapper.corruptJsons();
        // Simulate Relution tag message with one tag.
        scanner.simulateIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1);
        // Start trigger and scanner.
        trigger.start();
        scanner.startScanning();
        // Block test 500 milliseconds
        testBlocker.blockTest(500);
        // Verify that listener was called once with correct parameter.
        ArgumentCaptor<RelutionTagAction> actionArgument = ArgumentCaptor.forClass(RelutionTagAction.class);
        Mockito.verify(mockListener, Mockito.times(0)).onActionTriggered(actionArgument.capture());
    }
}
