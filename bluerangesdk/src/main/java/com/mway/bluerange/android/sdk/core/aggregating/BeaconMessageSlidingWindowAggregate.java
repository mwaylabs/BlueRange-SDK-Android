//
//  BeaconMessageSlidingWindowAggregate.java
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
import java.util.Timer;
import java.util.TimerTask;

public class BeaconMessageSlidingWindowAggregate extends BeaconMessageAggregate {

    public BeaconMessageSlidingWindowAggregate(BeaconMessage firstMessage, long aggregateDurationInMs) {
        super(firstMessage, aggregateDurationInMs);
    }

    public void removeOldMessages() {
        BeaconMessage recentMessage = messages.get(messages.size()-1);
        for (int i = 0; i < messages.size(); i++) {
            BeaconMessage message = messages.get(i);
            if (isOldMessage(message, recentMessage)) {
                messages.remove(i);
                i--;
            }
        }
    }

    private boolean isOldMessage(BeaconMessage message, BeaconMessage recentMessage) {
        long messageTimestampInMs = message.getTimestamp().getTime();
        long recentMessageTimestampInMs = recentMessage.getTimestamp().getTime();
        return (recentMessageTimestampInMs - messageTimestampInMs) > getAggregateDurationInMs();
    }

    @Override
    public Date getStartDate() {
        long endDateInMs = messages.get(messages.size()-1).getTimestamp().getTime();
        return new Date(endDateInMs - getAggregateDurationInMs());
    }

    @Override
    public Date getStopDate() {
        return messages.get(messages.size()-1).getTimestamp();
    }
}
