//
//  DummyBeaconMessagePersistor.java
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
