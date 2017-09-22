//
//  BeaconMessageAggregate.java
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

package com.mway.bluerange.android.sdk.core.aggregating;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A beacon message aggregate is a collection of equivalent beacon messages. An aggregate has a
 * predefined lifetime that is specified by a duration. When the end of the lifetime is reached,
 * all observers will be notified about this event.
 */
public abstract class BeaconMessageAggregate {

    protected List<BeaconMessage> messages = new ArrayList<>();
    private long aggregateDurationInMs;

    public BeaconMessageAggregate(BeaconMessage firstMessage, long aggregateDurationInMs) {
        this.aggregateDurationInMs = aggregateDurationInMs;
        initMessage(firstMessage);
    }

    private void initMessage(BeaconMessage firstMessage) {
        add(firstMessage);
    }

    public void add(BeaconMessage message) {
        messages.add(message);
    }

    public boolean fits(BeaconMessage message) {
        BeaconMessage firstMessage = messages.get(0);
        return message.equals(firstMessage);
    }

    public List<BeaconMessage> getMessages() {
        return messages;
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public abstract Date getStartDate();
    public abstract Date getStopDate();

    public long getAggregateDurationInMs() {
        return aggregateDurationInMs;
    }

    public void clear() {
        // May be overridden to destory timers.
    }
}
