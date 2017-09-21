//
//  BeaconMessagePacketAggregate.java
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
