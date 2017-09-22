//
//  IBeacon.java
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

import java.io.Serializable;
import java.util.UUID;

/**
 * An iBeacon is a triple containing a UUID, a major and a minor identifier.
 */
public class IBeacon implements Serializable {

    private UUID uuid;
    private int major;
    private int minor;

    // Default constructor necessary for JSON deserialization
    public IBeacon() {}

    public IBeacon(UUID uuid, int major, int minor) {
        super();
        init(uuid, major, minor);
    }

    private void init(UUID uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    @Override
    public String toString() {
        String str = "";
        UUID uuid = this.getUuid();
        int major = this.getMajor();
        int minor = this.getMinor();
        String outputString = "iBeacon: UUID = " + uuid + ", major = " + major + ", minor = " + minor;
        return str + outputString;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IBeacon)) {
            return false;
        }
        IBeacon beacon = (IBeacon) o;
        return beacon.getUuid().equals(this.getUuid()) &&
                beacon.getMajor() == this.getMajor() &&
                beacon.getMinor() == this.getMinor();
    }

    // Getters and setters

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }
}
