//
//  BeaconMessageQueuedStreamNodeTest.java
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
