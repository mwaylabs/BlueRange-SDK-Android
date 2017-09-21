//
//  DummyBeaconMessagePersistor.java
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

package com.mway.bluerange.android.sdk.core.logging.dummys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.logging.BeaconMessageLog;
import com.mway.bluerange.android.sdk.core.logging.BeaconMessagePersistor;

/**
 *
 */
public class DummyBeaconMessagePersistor implements BeaconMessagePersistor {

    private List<BeaconMessage> beaconMessages = new ArrayList<>();

    // Log iterator
    private class LogIterator implements Iterator<BeaconMessage> {

        private int cursor = 0;

        public LogIterator() {
            cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return cursor < beaconMessages.size();
        }

        @Override
        public BeaconMessage next() {
            BeaconMessage beaconMessage = beaconMessages.get(cursor);
            cursor++;
            return beaconMessage;
        }

        @Override
        public void remove() {
            // Not implemented
        }
    }

    @Override
    public void writeMessage(BeaconMessage beaconMessage) {
        this.beaconMessages.add(beaconMessage);
    }

    @Override
    public BeaconMessageLog readLog() {
        return new BeaconMessageLog(this.beaconMessages);
    }

    @Override
    public Iterator<BeaconMessage> getLogIterator() {
        return new LogIterator();
    }

    @Override
    public int getTotalMessages() {
        return this.beaconMessages.size();
    }

    @Override
    public void clearMessages() {
        this.beaconMessages.clear();
    }
}
