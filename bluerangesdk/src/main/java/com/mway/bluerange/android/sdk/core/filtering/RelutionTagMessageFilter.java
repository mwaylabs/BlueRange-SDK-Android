//
//  RelutionTagMessageFilter.java
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

package com.mway.bluerange.android.sdk.core.filtering;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;

import java.util.List;

/**
 * An Relution tag message filter filters iBeacon messages from a stream of beacon messages
 * delivered by all senders and sends the filtered stream to all receivers.
 */
public class RelutionTagMessageFilter extends BeaconMessageFilter {

    private long[] tags = null;

    public RelutionTagMessageFilter(BeaconMessageStreamNode senderNode) {
        super(senderNode);
    }

    public RelutionTagMessageFilter(List<BeaconMessageStreamNode> senderNodes) {
        super(senderNodes);
    }

    public RelutionTagMessageFilter(BeaconMessageStreamNode senderNode, long[] tags) {
        super(senderNode);
        this.tags = tags;
    }

    public RelutionTagMessageFilter(List<BeaconMessageStreamNode> senderNodes, long[] tags) {
        super(senderNodes);
        this.tags = tags;
    }

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        // 1. Filter by message types
        if (message instanceof RelutionTagMessageV1) {
            RelutionTagMessageV1 relutionTagMessage = (RelutionTagMessageV1) message;
            // 2. Filter tags if wanted
            if (!useTagFilter() || (useTagFilter() && messageContainsAtLeastOneMatchingTag(relutionTagMessage))) {
                for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
                    receiver.onReceivedMessage(this, message);
                }
            }
        }
    }

    private boolean useTagFilter() {
        return tags != null;
    }

    private boolean messageContainsAtLeastOneMatchingTag(RelutionTagMessageV1 relutionTagMessage) {
        for (long filterTag : tags) {
            for (long messageTag : relutionTagMessage.getTags()) {
                if (filterTag == messageTag) {
                    return true;
                }
            }
        }
        return false;
    }
}
