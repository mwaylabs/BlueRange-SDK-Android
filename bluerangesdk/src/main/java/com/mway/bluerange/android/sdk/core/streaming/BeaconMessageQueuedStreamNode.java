//
//  BeaconMessageQueuedStreamNode.java
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

package com.mway.bluerange.android.sdk.core.streaming;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A beacon message queued stream node is a node in a message processing graph that queues all
 * incoming beacon messages. The queue has a maximum size of {@link #maximumSize}. The messages
 * will not be delivered to any receiver. However, they can be pulled by calling the {@link
 * #pullBeaconMessage} method.
 */
public class BeaconMessageQueuedStreamNode extends BeaconMessageStreamNode {

    private final List<BeaconMessage> messageQueue = new ArrayList<BeaconMessage>();
    private static final int DEFAULT_MAXIMUM_SIZE = Integer.MAX_VALUE;
    private int maximumSize = DEFAULT_MAXIMUM_SIZE;

    public BeaconMessageQueuedStreamNode(BeaconMessageStreamNode sender) {super(sender);}
    public BeaconMessageQueuedStreamNode(List<BeaconMessageStreamNode> senders) {
        super(senders);
    }

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        pushBeaconMessage(message);
    }

    private void pushBeaconMessage(BeaconMessage beaconMessage) {
        // Do not add the message to the list, if the queue is full.
        if (messageQueue.size() >= maximumSize) {
            return;
        }
        // Adding the beacon messages is synchronized as it is typical for producer consumer scenarios.
        synchronized(this.messageQueue) {
            this.messageQueue.add(beaconMessage);
            // Wake up all threads that are waiting for incoming messages.
            this.messageQueue.notifyAll();
        }
    }

    public BeaconMessage pullBeaconMessage() throws InterruptedException {
        // Consumer of producer consumer pattern.
        synchronized (this.messageQueue) {
            while(messageQueue.isEmpty()) {
                this.messageQueue.wait();
            }
            BeaconMessage beaconMessage = messageQueue.get(0);
            messageQueue.remove(0);
            return beaconMessage;
        }
    }

    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    public int getMaximumSize() {
        return maximumSize;
    }
}
