//
//  BeaconMessageLogger.java
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

import android.content.Context;

import java.util.Iterator;

import com.mway.bluerange.android.sdk.core.streaming.BeaconMessagePassingStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;
import com.mway.bluerange.android.sdk.core.scanning.IBeaconMessageScanner;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

/**
 * A beacon message logger persistently logs a stream of {@link BeaconMessage}s delivered by the
 * senders and just passes the stream to all of its receivers. The log is saved in an energy
 * efficient manner. To read all messages from the log, you should call the {@link
 * #getLogIterator} or {@link #iterator} method, because calling the {@link #readLog} requires to
 * load the complete log in memory. The iterator, in contrast, reads the log in chunks and,
 * moreover, automatically updates the log, if it has changed while iterating over it. The
 * complete log will be deleted, if you call the {@link #clearLog} method.
 */
public class BeaconMessageLogger extends BeaconMessagePassingStreamNode implements Iterable<BeaconMessage> {

    ///// TRACING
    private final static String kLogTag = "BeaconMessageLogger";
    private ITracer tracer;

    ///// PERSISTOR
    private BeaconMessagePersistor persistor;

    /**
     * Creates a new instance of {@link BeaconMessageLogger} using the
     * preconfigured {@code beaconScanner}.
     * @param sender The sender from which we receive {@code BeaconMessage}s.
     * @param context The Android context used to log the BeaconMessages persistently.
     */
    public BeaconMessageLogger(BeaconMessageStreamNode sender, Context context) {
        this(sender, new BeaconMessagePersistorImpl(context), Tracer.getInstance());
    }

    /**
     * Creates a new instance using the preconfigured {@link IBeaconMessageScanner},
     * {@link BeaconMessagePersistor} and {@link ITracer}.
     * @param sender The {@link BeaconMessageStreamNode} from which we receive the messages.
     * @param persistor The {@link BeaconMessagePersistor} saves the log persistently
     *                  and provides access to the messages contained in the log.
     * @param tracer The {@link ITracer} used to log the output of this class.
     */
    public BeaconMessageLogger(BeaconMessageStreamNode sender, BeaconMessagePersistor persistor, ITracer tracer) {
        super(sender);
        this.persistor = persistor;
        this.tracer = tracer;
    }

    /**
     * Returns a new log object including all messages
     * that have been scanned.
     * @return a new {@link BeaconMessageLog} containing the logged messages.
     */
    public BeaconMessageLog readLog() {
        BeaconMessageLog log = persistor.readLog();
        return log;
    }

    /**
     * Returns a new iterator that can be used to iterate over all
     * {@link BeaconMessage}s contained in the log. The current
     * Iterator implementation can be used even if the log
     * is being changed while iterating. The iterator's
     * methods are implemented in a thread safe manner.
     * @return a new {@link Iterator}.
     */
    public Iterator<BeaconMessage> getLogIterator() {
        return persistor.getLogIterator();
    }

    /**
     * See {@link BeaconMessageLogger#getLogIterator()}
     * @return a new {@link Iterator}.
     */
    @Override
    public Iterator<BeaconMessage> iterator() {
        return getLogIterator();
    }

    public int getTotalMessagesInLog() {
        return this.persistor.getTotalMessages();
    }

    /**
     * Deletes all entries in the log.
     */
    public void clearLog() {
        persistor.clearMessages();
    }

    @Override
    protected void preprocessMessage(BeaconMessage message) {
        // Write
        persistor.writeMessage(message);
        // Trace
        //tracer.logDebug(kLogTag, message.toString());
    }

    @Override
    protected void postprocessMessage(BeaconMessage message) {

    }
}
