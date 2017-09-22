//
//  BeaconMessagePacketAggregate.java
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

public class BeaconMessagePacketAggregate extends BeaconMessageAggregate {

    private Date creationDate;
    private Date completionDate;
    private Timer timer;

    private List<BeaconMessagePacketAggregateObserver> observers = new ArrayList<>();

    public interface BeaconMessagePacketAggregateObserver {
        void onAggregateCompleted(BeaconMessagePacketAggregate aggregate);
    }

    public BeaconMessagePacketAggregate(BeaconMessage firstMessage, long aggregateDurationInMs) {
        super(firstMessage, aggregateDurationInMs);
        initCompletionDate(firstMessage);
        initCompletionTimer();
    }

    private void initCompletionDate(BeaconMessage firstMessage) {
        Date now = new Date();
        long timeNowInMs = now.getTime();
        // If first message has a lower timestamp than now, use this as the beginning.
        this.creationDate = new Date(Math.min(timeNowInMs, firstMessage.getTimestamp().getTime()));
        this.completionDate = new Date(this.creationDate.getTime() + getAggregateDurationInMs());
    }

    private void initCompletionTimer() {
        timer = new Timer("BeaconMessagePacketAggregate-CompletionTimer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (BeaconMessagePacketAggregateObserver observer : observers) {
                    observer.onAggregateCompleted(BeaconMessagePacketAggregate.this);
                }
            }
        }, this.completionDate);
    }

    public void addObserver(BeaconMessagePacketAggregateObserver observer) {
        this.observers.add(observer);
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    @Override
    public Date getStartDate() {
        return getCreationDate();
    }

    @Override
    public Date getStopDate() {
        return getCompletionDate();
    }

    @Override
    public void clear() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
