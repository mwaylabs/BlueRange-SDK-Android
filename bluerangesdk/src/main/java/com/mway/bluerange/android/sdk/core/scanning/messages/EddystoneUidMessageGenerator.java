//
//  EddystoneUidMessageGenerator.java
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
