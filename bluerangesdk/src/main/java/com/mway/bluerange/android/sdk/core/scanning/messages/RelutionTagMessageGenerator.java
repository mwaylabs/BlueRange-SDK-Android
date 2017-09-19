//
//  RelutionTagMessageGenerator.java
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

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

public class RelutionTagMessageGenerator extends EddystoneUidMessageGenerator {

    private boolean tagFilteringEnabled = false;
    private int[] tags;

    public RelutionTagMessageGenerator(String namespaceUid) {
        super(namespaceUid);
        this.tagFilteringEnabled = false;
    }

    public RelutionTagMessageGenerator(String namespaceUid, int[] tags) throws Exception {
        super(namespaceUid);
        this.tags = tags;
        this.tagFilteringEnabled = true;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        boolean isEddystoneUidValid = super.isValidBeacon(beacon);
        if (!isEddystoneUidValid) {
            return false;
        }
        // Check if at least on tag matches to the filter.
        if (tagFilteringEnabled) {
            boolean isValidBeacon = false;
            try {
                // If at least one tag matches, the beacon message
                // passes the filter.
                RelutionTagMessage message = new RelutionTagMessage(beacon);
                List<Integer> tagList = message.getTags();
                for (int i = 0; i < this.tags.length; i++) {
                    if (tagList.contains(this.tags[i])) {
                        isValidBeacon = true;
                        break;
                    }
                }
            } catch(Exception e) {
                isValidBeacon = false;
            }
            return isValidBeacon;
        } else {
            return true;
        }
    }

    @Override
    public EddystoneUidMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        RelutionTagMessage relutionTagMessage = new RelutionTagMessage(beacon);
        return relutionTagMessage;
    }

    @Override
    public boolean equals(Object o) {
        boolean isEddystoneUidEqual =  super.equals(o);
        if (!isEddystoneUidEqual) {
            return false;
        }
        if (!(o instanceof RelutionTagMessageGenerator)) {
            return false;
        }
        // Check if tags are equal.
        RelutionTagMessageGenerator generator = (RelutionTagMessageGenerator) o;
        // If at least one of the generators filters all Relution tags, it is considered
        // to be "the same" generator since it encloses the other one.
        if ((this.tagFilteringEnabled && generator.tagFilteringEnabled)
                && !tagsAreSame(generator.tags, this.tags)) {
            return false;
        }
        return true;
    }

    private boolean tagsAreSame(int[] tags1, int[] tags2) {
        if (tags1.length != tags2.length) {
            return false;
        }
        for (int i = 0; i < tags1.length; i++) {
            int tag1 = tags1[i];
            boolean containsTag1 = false;
            for (int j = 0; j < tags2.length; j++) {
                if (tag1 == tags2[j]) {
                    containsTag1 = true;
                }
            }
            if (!containsTag1) {
                return false;
            }
        }
        return true;
    }
}
