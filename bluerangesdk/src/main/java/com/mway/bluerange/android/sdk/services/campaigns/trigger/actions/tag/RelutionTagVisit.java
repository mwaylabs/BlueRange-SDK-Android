//
//  RelutionTagVisit.java
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

package com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag;

import java.util.Date;

/**
 *
 */
public class RelutionTagVisit {

    private String tag;
    private Date timestamp;
    private int rssi;

    public RelutionTagVisit(Date timestamp, String tag, int rssi) {
        this.timestamp = timestamp;
        this.tag = tag;
        this.rssi = rssi;
    }

    public String getTag() {
        return tag;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getRssi() {
        return rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelutionTagVisit)) {
            return false;
        }
        RelutionTagVisit tagObject = (RelutionTagVisit) o;
        return tagObject.getTag().equals(this.getTag());
    }

    @Override
    public String toString() {
        return tag;
    }
}
