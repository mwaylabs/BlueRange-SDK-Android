//
//  BeaconMessageScannerConfigTest.java
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
