//
//  BeaconMessageStreamNode.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base class of all message processing elements. Each instance of this class can be
 * interpreted as a node in a message stream processing graph, which can receive messages from a
 * list of incoming edges and a can send the messages to all its receivers. By using this class
 * as a base class of all message processing elements, it is possible to combine all elements to a
 * flexible message processing architecture.
 */
public abstract class BeaconMessageStreamNode implements BeaconMessageStreamNodeReceiver {

    private List<BeaconMessageStreamNode> senders = new ArrayList<>();
    private List<BeaconMessageStreamNodeReceiver> receivers = new ArrayList<>();

    public BeaconMessageStreamNode() {

    }

    public BeaconMessageStreamNode(BeaconMessageStreamNode sender) {
        addSender(sender);
    }

    public BeaconMessageStreamNode(List<BeaconMessageStreamNode> senders) {
        for (BeaconMessageStreamNode sender : senders) {
            addSender(sender);
        }
    }

    public void addSender(BeaconMessageStreamNode sender) {
        // Add the this instance as a receiver to the sender.
        sender.addReceiver(this);
        // Add it to the list of senders.
        this.senders.add(sender);
    }

    public void removeSender(BeaconMessageStreamNode sender){
        // Remove me from the receiver.
        sender.removeReceiver(this);
        // Remove me from the list of senders
        this.senders.remove(sender);
    }

    public List<BeaconMessageStreamNode> getSenders() {
        return this.senders;
    }

    public void addReceiver(BeaconMessageStreamNodeReceiver receiver) {
        this.receivers.add(receiver);
    }

    public void removeReceiver(BeaconMessageStreamNodeReceiver receiver) {
        this.receivers.remove(receiver);
    }

    public List<BeaconMessageStreamNodeReceiver> getReceivers() {
        return receivers;
    }

    @Override
    public void onMeshActive(BeaconMessageStreamNode senderNode) {
        // Default implementation is empty
    }

    @Override
    public void onMeshInactive(BeaconMessageStreamNode senderNode) {
        // Default implementation is empty
    }
}
