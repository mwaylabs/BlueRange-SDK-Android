//
//  IBeaconMessageFilterTest.java
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
