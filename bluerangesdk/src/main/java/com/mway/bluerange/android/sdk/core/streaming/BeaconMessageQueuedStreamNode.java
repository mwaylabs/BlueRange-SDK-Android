//
//  BeaconMessageQueuedStreamNode.java
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
