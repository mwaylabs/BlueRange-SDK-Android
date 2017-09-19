//
//  RelutionTagMessageV1.java
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

package com.mway.bluerange.android.sdk.core.scanning.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Relution Tag message is a Relution specific advertising message that can be delivered by
 * BlueRange SmartBeacons. A Relution tag message contains one or more tag identifiers. The
 * concept of Relution Tag messages is designed for apps that do not require internet access but
 * want to react to tags, that can be assigned dynamically to a beacon using the Relution platform.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("RelutionTagMessageV1")
public class RelutionTagMessageV1 extends BeaconMessage {

    // Relution Tag message V1
    public static final int RELUTION_TAG_MESSAGE_TYPE = 0x01;
    private List<Long> tags;

    // Default constructor necessary for JSON deserialization
    public RelutionTagMessageV1() {}

    public RelutionTagMessageV1(Beacon beacon, Region region, List<Long> tags) {
        super(beacon);
        this.tags = tags;
    }

    public RelutionTagMessageV1(long[] tags) {
        super();
        this.tags = new ArrayList<Long>();
        for(long tag : tags) {
            this.tags.add(tag);
        }
    }

    @Override
    protected String getDescription() {
        String str = "";
        List<Long> relutionTags = this.getTags();
        String outputString = "RelutionTagMessageV1: tags = ";
        for (int i = 0;i < relutionTags.size();i++) {
            if (i != 0) {
                outputString += ", ";
            }
            outputString += relutionTags.get(i);
        }
        return str + outputString;
    }

    @Override
    protected BeaconMessage copy() {
        long[] tags = new long[this.getTags().size()];
        for (int i = 0; i < this.getTags().size();i++) {
            tags[i] = this.getTags().get(i);
        }
        RelutionTagMessageV1 clonedMessage = new RelutionTagMessageV1(tags);
        return clonedMessage;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.getTags().toArray());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelutionTagMessageV1)) {
            return false;
        }
        RelutionTagMessageV1 beaconMessage = (RelutionTagMessageV1) o;
        return beaconMessage.getTags().containsAll(this.getTags())
                && this.getTags().containsAll(beaconMessage.getTags());
    }

    // Getters and setters

    public List<Long> getTags() {
        return tags;
    }

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }
}
