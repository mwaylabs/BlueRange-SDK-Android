//
//  BeaconMessageSlidingWindowAggregate.java
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
