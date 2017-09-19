//
//  BeaconMessageAggregate.java
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
