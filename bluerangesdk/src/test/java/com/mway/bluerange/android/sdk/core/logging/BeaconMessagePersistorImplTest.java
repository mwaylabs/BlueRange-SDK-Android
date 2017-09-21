//
//  BeaconMessagePersistorImplTest.java
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

package com.mway.bluerange.android.sdk.core.logging;

import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.core.logging.dummys.DummyFileAccessor;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.logging.dummys.DummyTracer;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 *
 */
public class BeaconMessagePersistorImplTest extends BeaconMessagePersistorTest {

    private DummyFileAccessor fileAccessor = new DummyFileAccessor();
    private DummyTracer tracer = new DummyTracer();

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    public BeaconMessagePersistorImpl getCut() {
        BeaconMessagePersistorImpl p = new BeaconMessagePersistorImpl(fileAccessor, 1);
        p.setTracer(tracer);
        return p;
    }

    @Override
    public void corruptAllFiles() {
        fileAccessor.corruptAllFiles();
    }

    @Test
    public void testLogIteratorIteratingOverCachedChunk() {
        // We need one message and a chunk size of 2!
        int numMessages = 1;
        int chunkSize = 2;
        this.persistor = new BeaconMessagePersistorImpl(fileAccessor, chunkSize);
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        // Iterator should iterate over the cached chunk
        List<BeaconMessage> readMessages = new ArrayList<BeaconMessage>();
        for (Iterator<BeaconMessage> iterator = this.persistor.getLogIterator(); iterator.hasNext();) {
            BeaconMessage message = iterator.next();
            readMessages.add(message);
        }
        Assert.assertEquals(writtenMessages, readMessages);
    }

    @Test
    public void testLogIteratorNextCalledTwice() {
        // We need one message and a chunk size of 2!
        int numMessages = 2;
        int chunkSize = 1;
        this.persistor = new BeaconMessagePersistorImpl(fileAccessor, chunkSize);
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        List<BeaconMessage> readMessages = new ArrayList<BeaconMessage>();
        for (Iterator<BeaconMessage> iterator = persistor.getLogIterator(); iterator.hasNext();) {
            // First chunk should be loaded!
            BeaconMessage message1 = iterator.next();
            // Second chunk should be loaded! Here the
            // preload mechanism of hasNext does not load the chunk.
            // That's why next should also load it.
            BeaconMessage message2 = iterator.next();
            readMessages.add(message1);
            readMessages.add(message2);
        }
        Assert.assertEquals(writtenMessages, readMessages);
    }

    @Test
    // This is just a test to see the number of bytes saved for each chunk.
    public void testChunkSpaceUsage() {

        // 1. Simulate extreme maximum space usage:
        // 2 hours sending 3 messages per second
        int numMessages = 2*60*60*3;
        // 2 hours a week 3 years and 2 messages/sec.
        // However in 3 years the log should be cleared at least once...
        //int numMessages = 3*53*2*60*60*2;

        int chunkSize = 100;
        DummyFileAccessor f = new DummyFileAccessor();
        this.persistor = new BeaconMessagePersistorImpl(f, chunkSize);
        BeaconMessagePersistorImpl p = (BeaconMessagePersistorImpl)persistor;
        p.setZippingEnabled(true);

        // Simulate alternatively sending iBeacon and Relution Tag messages.
        List<BeaconMessage> messages = new ArrayList<BeaconMessage>();
        for (int i = 0; i < numMessages; i+=2) {
            BeaconMessage message1 = getIBeacon();
            messages.add(message1);
        }
        for (int i = 1; i < numMessages; i+=2) {
            BeaconMessage message2 = getRelutionTagMessage();
            messages.add(message2);
        }
        for (BeaconMessage message : messages) {
            this.persistor.writeMessage(message);
        }

        int logSizeInBytes = p.getLogSizeInBytes();
        System.out.println("Log size for " + numMessages + " messages with chunk size "
                + chunkSize + ": " + logSizeInBytes/1024 + " kB.");

        // RESULTS: 1 hour //
        // Chunk size: 100
        //      Zipping disabled: Log size for 21600 messages with chunk size 100: 1282 kB.
        //      Zipping enabled:  Log size for 21600 messages with chunk size 100: 80 kB.

        // RESULTS: 3 years
        // Chunk size: 100
        //      Zipping disabled: Log size for 2289600 messages with chunk size 100: 135967 kB.
        //      Zipping enabled:  Log size for 2289600 messages with chunk size 100: 8471 kB.

        // Overall log size should not be greater than 10 MB
        Assert.assertTrue(logSizeInBytes < (10 * 1024 * 1024));
    }

    @Test
    public void testIfMaxLogSizeReachedDoNotAddMessages() {
        // 1. Write one message to the log
        int numMessages = 2;
        int maxLogSize = 1;
        BeaconMessagePersistorImpl p = (BeaconMessagePersistorImpl)persistor;
        p.setMaxLogSize(maxLogSize);
        List<BeaconMessage> writtenMessages = writeArbitraryMessages(numMessages);

        // 2. Iterator should read one message from the log
        List<BeaconMessage> readMessages = new ArrayList<>();
        for (Iterator<BeaconMessage> iterator = persistor.getLogIterator(); iterator.hasNext();) {
            BeaconMessage message = iterator.next();
            readMessages.add(message);
        }
        Assert.assertEquals(writtenMessages.subList(0,1), readMessages);
    }


    private BeaconMessage getIBeacon() {
        String uuid = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
        int major = 45;
        int minor = 1;
        IBeaconMessage message = new IBeaconMessage(UUID.fromString(uuid), major, minor);
        return message;
    }

    private BeaconMessage getRelutionTagMessage() {
        RelutionTagMessageV1 message = new RelutionTagMessageV1(new long[] {12, 15, 20});
        return message;
    }
}
