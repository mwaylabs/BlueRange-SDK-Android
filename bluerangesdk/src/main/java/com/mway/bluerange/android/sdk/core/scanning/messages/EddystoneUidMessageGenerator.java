//
//  EddystoneUidMessageGenerator.java
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
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * The beacon message generator class for Eddystone UID messages.
 */
public class EddystoneUidMessageGenerator extends EddystoneMessageGenerator {

    private boolean namespaceFilteringEnabled = false;
    private String namespace = null;
    private List<String> blacklistedNamespaces = new ArrayList<>();
    private boolean instanceFilteringEnabled = false;
    private String instance = null;

    public EddystoneUidMessageGenerator() {
        this.namespaceFilteringEnabled = false;
        this.instanceFilteringEnabled = false;
    }

    public EddystoneUidMessageGenerator(String namespace) {
        this.namespaceFilteringEnabled = true;
        this.instanceFilteringEnabled = false;
        this.namespace = EddystoneUidMessage.getNormalizedStringIdentifier(namespace,
                EddystoneUidMessage.NAMESPACE_BYTE_LENGTH);
    }

    public EddystoneUidMessageGenerator(String namespace, String instance) {
        this.namespaceFilteringEnabled = true;
        this.instanceFilteringEnabled = true;
        this.namespace = EddystoneUidMessage.getNormalizedStringIdentifier(namespace,
                EddystoneUidMessage.NAMESPACE_BYTE_LENGTH);
        this.instance = EddystoneUidMessage.getNormalizedStringIdentifier(instance,
                EddystoneUidMessage.INSTANCE_BYTE_LENGTH);
    }

    @Override
    public String getBeaconLayout() throws Exception {
        String layout = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19,d:0-1,d:2-2";
        return layout;
    }

    @Override
    public Region getRegion() throws Exception {
        Identifier namespaceIdentifier;
        Identifier instanceIdentifier;

        if (this.namespaceFilteringEnabled) {
            byte[] namespaceBytes = getIdentifierBytes(this.namespace);
            namespaceIdentifier = Identifier.fromBytes(
                    namespaceBytes, 0, namespaceBytes.length, false);
        } else {
            namespaceIdentifier = null;
        }

        if (this.instanceFilteringEnabled) {
            byte[] instanceBytes = getIdentifierBytes(this.instance);
            instanceIdentifier =  Identifier.fromBytes(
                    instanceBytes, 0, instanceBytes.length, false);
        } else {
            instanceIdentifier = null;
        }

        Region region = new Region(this.toString(), namespaceIdentifier, instanceIdentifier, null);
        return region;
    }

    private byte[] getIdentifierBytes(String hexString) {
        // Bytes will be saved in big endian order
        byte[] bytes = ByteArrayConverter.hexStringToByteArray(hexString);
        return bytes;
    }

    @Override
    public boolean isValidBeacon(Beacon beacon) {
        boolean isValidBeacon = super.isValidBeacon(beacon);

        // Message must be a an Eddystone message.
        if (!isValidBeacon) {
            return false;
        }

        try {
            // Message must be of type Eddystone URL
            List<Long> dataFields = beacon.getDataFields();
            long eddystoneFrameType = dataFields.get(1);
            if (eddystoneFrameType != EDDY_FRAME_UID) {
                return false;
            }

            String actualNamespace = EddystoneUidMessage.getNormalizedStringIdentifier(
                    EddystoneUidMessage.getHexStringIdentifier(beacon.getId1()),
                    EddystoneUidMessage.NAMESPACE_BYTE_LENGTH);
            String actualInstance = EddystoneUidMessage.getNormalizedStringIdentifier(
                    EddystoneUidMessage.getHexStringIdentifier(beacon.getId2()),
                    EddystoneUidMessage.INSTANCE_BYTE_LENGTH);

            String acceptedNamespace = EddystoneUidMessage.getNormalizedStringIdentifier(
                    this.namespace,
                    EddystoneUidMessage.NAMESPACE_BYTE_LENGTH);
            String acceptedInstance = EddystoneUidMessage.getNormalizedStringIdentifier(
                    this.instance,
                    EddystoneUidMessage.INSTANCE_BYTE_LENGTH);

            isValidBeacon = true;

            if (namespaceFilteringEnabled && (!actualNamespace.equals(acceptedNamespace))) {
                isValidBeacon = false;
            }

            // Consider blacklisted namespaces as well
            if (blacklistedNamespaces.contains(actualNamespace)) {
                isValidBeacon = false;
            }

            if (instanceFilteringEnabled && (!actualInstance.equals(acceptedInstance))) {
                isValidBeacon = false;
            }
        } catch(Exception e) {
            isValidBeacon = false;
        }
        return isValidBeacon;
    }

    @Override
    public EddystoneUidMessage constructBeaconMessage(Beacon beacon, Region region) throws Exception {
        EddystoneUidMessage eddystoneUidMessage = new EddystoneUidMessage(beacon);
        return eddystoneUidMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof  EddystoneUidMessageGenerator)) {
            return false;
        }
        EddystoneUidMessageGenerator generator = (EddystoneUidMessageGenerator) o;

        if ((this.namespaceFilteringEnabled && generator.namespaceFilteringEnabled)
                && (!generator.namespace.equals(namespace))) {
            return false;
        }

        if ((this.instanceFilteringEnabled && generator.instanceFilteringEnabled)
                && (!generator.instance.equals(instance))) {
            return false;
        }

        return true;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getInstance() {
        return instance;
    }

    public void blacklistNamespace(String namespaceUid) {
        blacklistedNamespaces.add(namespaceUid);
    }
}
