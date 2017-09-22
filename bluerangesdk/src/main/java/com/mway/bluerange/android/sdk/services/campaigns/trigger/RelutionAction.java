//
//  RelutionAction.java
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

import com.mway.bluerange.android.sdk.core.triggering.BeaconAction;

import java.util.Date;

/**
 * Created by Ehrlich on 20.08.2016.
 */
public class RelutionAction extends BeaconAction {
    public static final String ACTION_ID_PARAMETER = "uuid";
    public static final String TYPE_PARAMETER = "type";
    public static final String VALID_UNTIL_PARAMETER = "validUntil";
    public static final String POSTPONE_PARAMETER = "postpone";
    public static final String MIN_STAY_PARAMETER = "minStay";
    public static final String REPEAT_EVERY_PARAMETER = "repeatEvery";
    public static final String DISTANCE_THRESHOLD_PARAMETER = "distanceThreshold";

    /**
     * The campaign the action is defined.
     */
    private RelutionCampaign campaign;

    public RelutionAction(String actionId) {
        super(actionId);
    }

    public void setCampaign(RelutionCampaign campaign) {
        this.campaign = campaign;
    }

    public RelutionCampaign getCampaign() {
        return campaign;
    }

    public boolean isCampaignExpired() {
        Date campaignValidityEnds = campaign.getEndsDate();
        Date now = new Date();
        return now.after(campaignValidityEnds);
    }

    public boolean isCampaignInactive() {
        Date campaignValidityBegins = campaign.getBeginsDate();
        Date now = new Date();
        return now.before(campaignValidityBegins);
    }

    @Override
    public boolean isExpired() {
        return super.isExpired() || isCampaignExpired() || isCampaignInactive();
    }
}
