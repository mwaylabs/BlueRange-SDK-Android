//
//  BeaconMessagePassingStreamNode.java
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

import java.util.List;

/**
 * A beacon message passing stream node is a node in a message processing graph that passes the
 * incoming messages from all senders to all receivers. Before they are passed, however, an
 * instance of a subclass can override the {@link #preprocessMessage} method to modify or analyze
 * the data before passing it to its receivers. After the message has been passed to its
 * receivers, the {@link #postprocessMessage} method will be called (which can also be overridden
 * by a subclass) to do some postprocessing.
 */
public abstract class BeaconMessagePassingStreamNode extends BeaconMessageStreamNode {

    public BeaconMessagePassingStreamNode() {}
    public BeaconMessagePassingStreamNode(BeaconMessageStreamNode sender) {super(sender);}
    public BeaconMessagePassingStreamNode(List<BeaconMessageStreamNode> senders) {
        super(senders);
    }

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        // 1. Preprocessing
        preprocessMessage(message);
        // 2. Delegating
        passMessageToReceivers(message);
        // 3. Postprocessing
        postprocessMessage(message);
    }

    private void passMessageToReceivers(BeaconMessage message) {
        for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
            receiver.onReceivedMessage(this, message);
        }
    }

    /**
     * This method is called right before a beacon message is passed to the receivers.
     * @param message The received message that is going to be passed to the receivers.
     */
    protected void preprocessMessage(BeaconMessage message) {
        // Default implementation is empty.
    }

    /**
     * This method is called right after a beacon message was passed to the receivers.
     * @param message The received message that was passed to the receivers.
     */
    protected void postprocessMessage(BeaconMessage message) {
        // Default implementation is empty.
    }
}
