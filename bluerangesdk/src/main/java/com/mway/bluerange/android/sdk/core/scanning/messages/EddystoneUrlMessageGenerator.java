//
//  EddystoneUrlMessageGenerator.java
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

import com.mway.bluerange.android.sdk.utils.string.ByteArrayConverter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.List;

/**
 * The beacon message generator class for Eddystone URL messages.
 */
public class EddystoneUrlMessageGenerator extends EddystoneMessageGenerator {

    private boolean urlFilteringEnabled = false;
    private String url = null;

    public EddystoneUrlMessageGenerator() {
        this.urlFilteringEnabled = false;
    }

    public EddystoneUrlMessageGenerator(String url) {
        this.urlFilteringEnabled = true;
        this.url = url;
    }

    @Override
    public String getBeaconLayout() throws Exception {
        String layout = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v,d:0-1,d:2-2";
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        Identifier urlIdentifier;

        if (this.urlFilteringEnabled) {
            byte[] urlBytes = EddystoneUrlMessage.getUrlBytesFromString(this.url);
            urlIdentifier = Identifier.fromBytes(
                    urlBytes, 0, urlBytes.length, false);
        } else {
            urlIdentifier = null;
        }

        Region region = new Region(this.toString(), urlIdentifier, null, null);
        return region;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        boolean isValidBeacon = super.isValidBeacon(beacon);

        // Message must be an Eddystone message.
        if (!isValidBeacon) {
            return false;
        }

        try {
            // Message must be of type Eddystone URL
            List<Long> dataFields = beacon.getDataFields();
            long eddystoneFrameType = dataFields.get(1);
            if (eddystoneFrameType != EDDY_FRAME_URL) {
                return false;
            }

            String actualUrl = EddystoneUrlMessage.getUrlStringFromBytes(beacon.getId1().toByteArray());
            String acceptedUrl = this.url;

            isValidBeacon = true;

            if (urlFilteringEnabled && (!actualUrl.equals(acceptedUrl))) {
                isValidBeacon = false;
            }
        } catch(Exception e) {
            isValidBeacon = false;
        }
        return isValidBeacon;
    }

    @Override
    public EddystoneUrlMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        EddystoneUrlMessage eddystoneUrlMessage = new EddystoneUrlMessage(beacon);
        return eddystoneUrlMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  EddystoneUrlMessageGenerator)) {
            return false;
        }
        EddystoneUrlMessageGenerator generator = (EddystoneUrlMessageGenerator) o;

        if ((this.urlFilteringEnabled && generator.urlFilteringEnabled)
                && (!generator.url.equals(this.url))) {
            return false;
        }

        return true;
    }
}
