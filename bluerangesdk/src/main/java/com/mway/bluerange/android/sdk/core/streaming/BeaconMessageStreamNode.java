//
//  BeaconMessageStreamNode.java
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
