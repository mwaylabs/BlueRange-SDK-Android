//
//  RelutionAction.java
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
