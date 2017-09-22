//
//  BeaconMessagePersistorTest.java
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

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 *
 */
public abstract class BeaconMessagePersistorTest {

    protected BeaconMessagePersistor persistor;

    @Before
    public void setUp() {
        this.persistor = getCut();
    }

    public abstract BeaconMessagePersistor getCut();

    @Test
    public void testLogIteratorWithZeroMessages() {
        // 1. Write no message to the log
        // 2. Iterator should read one message from the log
        Iterator<BeaconMessage> iterator = persistor.getLogIterator();
        boolean hasNext = iterator.hasNext();
        Assert.assertFalse(hasNext);
    }

    @Test
    public void testLogIteratorWithOneMessage() {
        // 1. Write one message to the log
        int numMessages = 1;
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        // 2. Iterator should read one message from the log
        List<BeaconMessage> readMessages = new ArrayList<>();
        for (Iterator<BeaconMessage> iterator = persistor.getLogIterator(); iterator.hasNext();) {
            BeaconMessage message = iterator.next();
            readMessages.add(message);
        }
        Assert.assertEquals(writtenMessages, readMessages);
    }

    @Test
    public void testEmptyLogAfterClearLog() {
        int numMessages = 1;
        List<BeaconMessage> writtenBeaconMessages = writeArbitraryMessages(numMessages);
        persistor.clearMessages();
        BeaconMessageLog log = persistor.readLog();
        List<BeaconMessage> readMessages = log.getBeaconMessages();
        Assert.assertEquals(0, readMessages.size());
    }

    @Test
    public void testNoDirtyReadWithLogIterator() {
        int numMessages = 1;
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        // Whenever hasNext returns true, it should
        // already have preloaded the next chunk because
        // the chunk could be removed right after calling
        // the hasNext method.
        // A clearMessages call right at the beginning of
        // the iteration should not result in an exception!
        List<BeaconMessage> readMessages = new ArrayList<BeaconMessage>();
        for (Iterator<BeaconMessage> iterator = persistor.getLogIterator(); iterator.hasNext();) {
            persistor.clearMessages();
            BeaconMessage message = iterator.next();
            readMessages.add(message);
        }

        Assert.assertEquals(writtenMessages, readMessages);
    }

    @Test(expected = NoSuchElementException.class)
    public void testLogIteratorNoSuchElementException() {
        // Only one message!
        int numMessages = 1;
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        Iterator<BeaconMessage> iterator = persistor.getLogIterator();
        // Call next twice!
        iterator.next();
        iterator.next();
        // Check if NoSuchElementException was thrown.
    }

    @Test
    public void testLogIteratorAccessingCorruptFile() {
        // 1. Simulate no message
        int numMessages = 1;
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        // 2. Corrupt all files
        corruptAllFiles();

        // 3. Iterator should return false on hasNext call.
        Iterator<BeaconMessage> iterator = persistor.getLogIterator();
        boolean hasNext = iterator.hasNext();
        Assert.assertFalse(hasNext);
    }

    @Test
    public void testGetTotalMessagesInLog() {
        // 1. Simulate no message
        int numMessages = 3;
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        // get total messages
        int actualNumMessages = persistor.getTotalMessages();

        // Validate
        Assert.assertEquals(numMessages, actualNumMessages);
    }

    public abstract void corruptAllFiles();

    protected List<BeaconMessage> writeArbitraryMessages(int numberOfMessages) {
        List<BeaconMessage> beaconMessages = new ArrayList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            IBeaconMessage beaconMessage = getArbitraryIBeacon();
            beaconMessages.add(beaconMessage);
            this.persistor.writeMessage(beaconMessage);
        }
        return beaconMessages;
    }

    private IBeaconMessage getArbitraryIBeacon() {
        String uuid = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
        int major = 45;
        int minor = 1;
        IBeaconMessage message = new IBeaconMessage(UUID.fromString(uuid), major, minor);
        return message;
    }
}
