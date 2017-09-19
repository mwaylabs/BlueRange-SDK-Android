//
//  BeaconMessageLogger.java
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
