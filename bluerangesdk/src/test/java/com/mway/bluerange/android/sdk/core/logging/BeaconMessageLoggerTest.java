//
//  BeaconMessageLoggerTest.java
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

package com.mway.bluerange.android.sdk.core.logging;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.core.logging.dummys.DummyFileAccessor;
import com.mway.bluerange.android.sdk.core.logging.dummys.DummyTracer;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class BeaconMessageLoggerTest {

    private BeaconMessageLogger logger;
    private BeaconMessageScannerSimulator beaconScanner;
    private DummyFileAccessor fileAccessor;
    private BeaconMessagePersistorImpl persistor;
    private DummyTracer tracer;
    private BeaconMessageStreamNodeReceiver listenerMock;

    @Before
    public void setUp() {
        this.beaconScanner = new BeaconMessageScannerSimulator();
        this.fileAccessor = new DummyFileAccessor();
        this.persistor = new BeaconMessagePersistorImpl(fileAccessor, 1);
        this.tracer = new DummyTracer();
        this.logger = new BeaconMessageLogger(beaconScanner, persistor, tracer);
        this.listenerMock = Mockito.mock(BeaconMessageStreamNodeReceiver.class);
        logger.addReceiver(listenerMock);
    }

    // Writing and reading from log
    @Test
    public void testReadLogWithMessagesThatFillExactlyOneChunk() {
        int numMessages = 2;
        List<BeaconMessage> writtenMessages = simulateArbitraryMessages(numMessages);
        beaconScanner.startScanning();
        beaconScanner.stopScanning();
        List<BeaconMessage> readMessages = getLoggedBeaconMessages();
        Assert.assertEquals(writtenMessages, readMessages);
    }

    @Test
    // Notification
    public void testOneMessageSaveNotification() {
        int numMessages = 1;
        simulateArbitraryMessages(numMessages);
        beaconScanner.startScanning();
        verifyOnMessageSavedCalled(numMessages);
    }

    @Test
    // Tests log iterator
    public void testLogIterator() {
        int numMessages = 1;
        List<BeaconMessage> writtenMessages = simulateArbitraryMessages(numMessages);
        beaconScanner.startScanning();
        beaconScanner.stopScanning();
        List<BeaconMessage> readMessages = new ArrayList<BeaconMessage>();
        for (Iterator<BeaconMessage> iterator = logger.getLogIterator(); iterator.hasNext();) {
            BeaconMessage message = iterator.next();
            readMessages.add(message);
        }
        Assert.assertEquals(writtenMessages, readMessages);
    }

    @Test
    // Test for each iterator
    public void testForEachIterator() {
        int numMessages = 1;
        List<BeaconMessage> writtenMessages = simulateArbitraryMessages(numMessages);
        beaconScanner.startScanning();
        beaconScanner.stopScanning();
        List<BeaconMessage> readMessages = new ArrayList<BeaconMessage>();
        for (BeaconMessage message : logger) {
            readMessages.add(message);
        }
        Assert.assertEquals(writtenMessages, readMessages);
    }

    private List<BeaconMessage> simulateArbitraryMessages(int numberOfMessages) {
        List<BeaconMessage> beaconMessages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            IBeaconMessage beaconMessage = simulateArbitraryIBeacon();
            beaconMessages.add(beaconMessage);
        }
        return beaconMessages;
    }

    private IBeaconMessage simulateArbitraryIBeacon() {
        String uuid = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
        int major = 45;
        int minor = 1;
        IBeaconMessage message = new IBeaconMessage(UUID.fromString(uuid), major, minor);
        beaconScanner.simulateIBeacon(uuid, major, minor);
        return message;
    }

    private List<BeaconMessage> getLoggedBeaconMessages() {
        BeaconMessageLog log = logger.readLog();
        List<BeaconMessage> readMessages = log.getBeaconMessages();
        return readMessages;
    }

    private void verifyOnMessageSavedCalled(int numCalls) {
        Mockito.verify(listenerMock, times(numCalls)).onReceivedMessage(
                any(BeaconMessageLogger.class), any(BeaconMessage.class));
    }
}
