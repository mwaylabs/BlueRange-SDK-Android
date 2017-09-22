//
//  RelutionCampaign.java
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

package com.mway.bluerange.android.sdk.services.campaigns.trigger;

import java.util.Date;

/**
 *
 */
public class RelutionCampaign {
    public static final String kCampaignsParameter = "results";
    public static final String kActionsParameter = "actions";
    public static final String kBeginsParameter = "beginsAt";
    public static final String kEndsParameter = "endsAt";
    public static final long kBeginsDefaultValue = 0l;
    public static final long kEndsDefaultValue = 2085816501000l;
    private final Date beginsDate;
    private final Date endsDate;

    public RelutionCampaign(Date beginsDate, Date endsDate) {
        this.beginsDate = beginsDate;
        this.endsDate = endsDate;
    }

    public Date getBeginsDate() {
        return beginsDate;
    }

    public Date getEndsDate() {
        return endsDate;
    }
}
