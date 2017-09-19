//
//  BeaconMessageAggregator.java
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

import com.mway.bluerange.android.sdk.core.aggregating.averaging.LinearWeightedMovingAverageFilter;
import com.mway.bluerange.android.sdk.core.aggregating.averaging.MovingAverageFilter;
import com.mway.bluerange.android.sdk.core.aggregating.averaging.SimpleMovingAverageFilter;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;

import java.util.ArrayList;
import java.util.List;

/**
 * A beacon message aggregator is a node in a message processing graph that merges equivalent
 * beacon messages. Whenever the aggregator receives a message from a sender, the message will be
 * added to a matching beacon message aggregate or, if a such an aggregate does not exist, it
 * will be added to a new aggregate instance. Each aggregate has a predefined duration. After
 * this duration all messages contained inside an aggregate will be combined to a new beacon
 * message that is identical to the first message in the aggregate. However to make further
 * message processing more stable, message properties like the RSSI value will be averaged by
 * using a moving average filter. The resulting beacon message will be sent to all receivers of
 * the node.
 */
public class BeaconMessageAggregator extends BeaconMessageStreamNode
        implements BeaconMessagePacketAggregate.BeaconMessagePacketAggregateObserver {

    // Tracing
    private ITracer tracer;
    private static final String LOG_TAG = "Aggregator";
    // Mode
    private AggregationMode aggregationMode;
    // Configuration
    private static final long DEFAULT_AGGREGATE_DURATION_IN_MS = 1000;
    private long aggregateDurationInMs = DEFAULT_AGGREGATE_DURATION_IN_MS;
    // Aggregates
    private final List<BeaconMessageAggregate> aggregates = new ArrayList<>();
    // AverageFilter
    private MovingAverageFilter averageFilter = new LinearWeightedMovingAverageFilter(0.3f);
    // GarbageCollector
    private Thread garbageCollectorThread;

    public enum AggregationMode {
        PACEKT,
        SLIDING_WINDOW
    }

    public BeaconMessageAggregator(ITracer tracer, BeaconMessageStreamNode sender) {
        super(sender);
        init(tracer);
    }

    public BeaconMessageAggregator(ITracer tracer, List<BeaconMessageStreamNode> senders) {
        super(senders);
        init(tracer);
    }

    private void init(ITracer tracer) {
        this.tracer = tracer;
        this.setAggregationMode(AggregationMode.PACEKT);
        startAggregateGarbageCollector();
    }

    private void startAggregateGarbageCollector() {
        garbageCollectorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        removeGarbage();
                        Thread.sleep(10 * 1000);
                    }
                } catch (InterruptedException e) {
                    // On interrupt, we stop this thread.
                }
            }
        });
        garbageCollectorThread.start();
    }

    private void removeGarbage() {
        synchronized (aggregates) {
            for (int i = 0; i < aggregates.size(); i++) {
                if (this.aggregationMode == AggregationMode.SLIDING_WINDOW) {
                    BeaconMessageAggregate aggregate = aggregates.get(i);
                    BeaconMessageSlidingWindowAggregate slidingWindowAggregate =
                            (BeaconMessageSlidingWindowAggregate) aggregate;
                    slidingWindowAggregate.removeOldMessages();
                    if (slidingWindowAggregate.isEmpty()) {
                        aggregates.remove(slidingWindowAggregate);
                    }
                }
            }
        }
    }

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        tracer.logDebug(LOG_TAG, "Aggregator received message with RSSI ." + message.getRssi());
        synchronized (aggregates) {
            BeaconMessageAggregate aggregate = findAggregateForMessage(message);
            if (aggregate != null) {
                aggregate.add(message);
            } else {
                if (this.aggregationMode == AggregationMode.PACEKT) {
                    BeaconMessagePacketAggregate packetAggregate
                            = new BeaconMessagePacketAggregate(message, aggregateDurationInMs);
                    packetAggregate.addObserver(this);
                    aggregates.add(packetAggregate);
                    aggregate = packetAggregate;
                } else if(this.aggregationMode == AggregationMode.SLIDING_WINDOW) {
                    BeaconMessageSlidingWindowAggregate slidingWindowAggregate
                            = new BeaconMessageSlidingWindowAggregate(message, aggregateDurationInMs);
                    aggregate = slidingWindowAggregate;
                    aggregates.add(slidingWindowAggregate);
                }
            }

            // Instant reaction in sliding window mode.
            if(this.aggregationMode == AggregationMode.SLIDING_WINDOW) {
                BeaconMessageSlidingWindowAggregate slidingWindowAggregate =
                        (BeaconMessageSlidingWindowAggregate) aggregate;
                handleSlidingWindowMessage(slidingWindowAggregate);
            }
        }
    }

    private BeaconMessageAggregate findAggregateForMessage(BeaconMessage message) {
        for (BeaconMessageAggregate aggregate : aggregates) {
            if (aggregate.fits(message)) {
                return aggregate;
            }
        }
        return null;
    }

    @Override
    public void onAggregateCompleted(BeaconMessagePacketAggregate aggregate) {
        synchronized (aggregates) {
            publishAggregatedMessageForAggregate(aggregate);
            removeAggregate(aggregate);
        }
    }

    public void handleSlidingWindowMessage(BeaconMessageSlidingWindowAggregate aggregate) {
        synchronized (aggregates) {
            aggregate.removeOldMessages();
            publishAggregatedMessageForAggregate(aggregate);
        }
    }

    private void publishAggregatedMessageForAggregate(BeaconMessageAggregate aggregate) {
        BeaconMessage aggregatedMessage = createAggregatedMessage(aggregate);
        sendToReceivers(aggregatedMessage);
        tracer.logDebug(LOG_TAG, "Sent aggregate message with RSSI " + aggregatedMessage
                .getRssi());
    }

    private BeaconMessage createAggregatedMessage(BeaconMessageAggregate aggregate) {
        // Use first message as prototype.
        BeaconMessage aggregatedMessage = aggregate.getMessages().get(0).clone();
        // Merge messages property of all messages in this aggregate
        int avgRssi = (int)getAverageRssi(aggregate);
        aggregatedMessage.setRssi(avgRssi);
        // Return flattenedMessage
        return aggregatedMessage;
    }

    private float getAverageRssi(BeaconMessageAggregate aggregate) {
        long minTime = aggregate.getStartDate().getTime();
        long maxTime = aggregate.getStopDate().getTime();
        List<Long> timePoints = new ArrayList<>();
        List<Float> values = new ArrayList<>();

        for (BeaconMessage message : aggregate.getMessages()) {
            timePoints.add(message.getTimestamp().getTime());
            values.add((float)message.getRssi());
        }

        float average = this.averageFilter.getAverage(minTime, maxTime, timePoints, values);
        return average;
    }

    private void sendToReceivers(BeaconMessage aggregatedMessage) {
        for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
            receiver.onReceivedMessage(this, aggregatedMessage);
        }
    }

    public void stop() {
        synchronized (aggregates) {
            for (BeaconMessageAggregate aggregate : aggregates) {
                aggregate.clear();
            }
            aggregates.clear();
        }
        garbageCollectorThread.interrupt();
    }

    private void removeAggregate(BeaconMessageAggregate aggregate) {
        aggregate.clear();
        aggregates.remove(aggregate);
    }

    public long getAggregateDurationInMs() {
        return aggregateDurationInMs;
    }

    public void setAggregateDurationInMs(long aggregateDurationInMs) {
        this.aggregateDurationInMs = aggregateDurationInMs;
    }

    public MovingAverageFilter getAverageFilter() {
        return averageFilter;
    }

    public void setAverageFilter(MovingAverageFilter averageFilter) {
        this.averageFilter = averageFilter;
    }

    public void setAggregationMode(AggregationMode mode) {
        this.aggregationMode = mode;
    }

    public AggregationMode getAggregationMode() {
        return this.aggregationMode;
    }
}
