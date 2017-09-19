//
//  EddystoneUidMessage.java
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

import android.annotation.SuppressLint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

/**
 * Represents an Eddystone UID message containing a Namespace UID
 * and an instance identifier.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("EddystoneUidMessage")
public class EddystoneUidMessage extends EddystoneMessage {

    public static final int NAMESPACE_BYTE_LENGTH = 10;
    public static final int INSTANCE_BYTE_LENGTH = 6;
    protected String namespaceUid;
    protected String instanceId;

    // Default constructor necessary for JSON deserialization
    public EddystoneUidMessage() {}

    public EddystoneUidMessage(String namespaceUid, String instanceId) {
        super();
        this.namespaceUid = getNormalizedStringIdentifier(namespaceUid, NAMESPACE_BYTE_LENGTH);
        this.instanceId = getNormalizedStringIdentifier(instanceId, INSTANCE_BYTE_LENGTH);
    }

    public EddystoneUidMessage(Beacon beacon) {
        super(beacon);
        this.namespaceUid = getNormalizedStringIdentifier(getHexStringIdentifier(beacon.getId1()),
                NAMESPACE_BYTE_LENGTH);
        this.instanceId = getNormalizedStringIdentifier(getHexStringIdentifier(beacon.getId2()),
                INSTANCE_BYTE_LENGTH);
    }

    protected static String getHexStringIdentifier(Identifier identifier) {
        String hexString = identifier.toHexString();
        String hexPrefixRemovedString = hexString.substring(2, hexString.length());
        return hexPrefixRemovedString;
    }

    @SuppressLint("DefaultLocale")
    protected static String getNormalizedStringIdentifier(String identifier, int targetByteLength) {
        String result = "" + identifier;
        int targetStringLength = targetByteLength*2;
        int numZerosToAdd = targetStringLength - result.length();
        for (int i = 0; i < numZerosToAdd; i++) {
            result = "0" + result;
        }
        result = result.toUpperCase();
        return result;
    }

    @Override
    protected BeaconMessage copy() {
        return new EddystoneUidMessage(namespaceUid, instanceId);
    }

    @Override
    public int hashCode() {
        return this.namespaceUid.hashCode() + this.instanceId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EddystoneUidMessage)) {
            return false;
        }
        EddystoneUidMessage beaconMessage = (EddystoneUidMessage) o;
        return beaconMessage.getNamespaceUid().equals(this.getNamespaceUid())
                && beaconMessage.getInstanceId().equals(this.getInstanceId());
    }

    private String getPrettyIdentifier(String identifier) {
        int numSignsToRemove = 0;
        for (int i = 0; i < identifier.length(); i++) {
            if (identifier.charAt(i) == '0') {
                numSignsToRemove++;
            } else {
                break;
            }
        }
        // Do not remove zeros if it belongs to a non-zero byte in the string.
        // -> Remove only tuples of zeros.
        if (numSignsToRemove % 2  != 0) {
            numSignsToRemove--;
        }
        return identifier.substring(numSignsToRemove);
    }

    @Override
    protected String getDescription() {
        return "Eddystone UID: Namespace: " + this.namespaceUid + ", instance: " + this.instanceId;
    }

    // Getters and setters

    /**
     * Returns the Namespace UID of the Eddystone UID message. E.g. "65AC11A8F8C51FF6476F"
     * @return the namespace UID.
     */
    public String getNamespaceUid() {
        return getPrettyIdentifier(this.namespaceUid);
    }

    public void setNamespaceUid(String namespaceUid) {
        this.namespaceUid = namespaceUid;
    }

    /**
     * Returns the instance id of the Eddystone UID message. E.g. "1F2A"
     * @return The instance id.
     */
    public String getInstanceId() {
        return getPrettyIdentifier(this.instanceId);
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
