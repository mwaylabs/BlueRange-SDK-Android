//
//  BeaconAction.java
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

package com.mway.bluerange.android.sdk.core.triggering;

import com.mway.bluerange.android.sdk.core.distancing.AnalyticalDistanceEstimator;
import com.mway.bluerange.android.sdk.core.distancing.DistanceEstimator;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

import java.util.Date;

/**
 *
 */
public class BeaconAction {

    private static DistanceEstimator distanceEstimator = new AnalyticalDistanceEstimator();

    /**
     * The beacon message that initiated the instantiation of this action.
     */
    private BeaconMessage sourceBeaconMessage = null;

    private Date creationDate;

    /**
     * The unique identifier for the action.
     */
    private String actionId;

    public static final long MIN_VALIDITY_BEGINS = 0;
    /**
     * The time when the action will become active. Default: new Date(MIN_VALIDITY_BEGINS)
     */
    private Date validityBegins = new Date(MIN_VALIDITY_BEGINS);

    public static final long MAX_VALIDITY_ENDS = 2085816501000l;
    /**
     * The time, when the action will become inactive. Default: new Date(MAX_VALIDITY_ENDS)
     */
    private Date validityEnds = new Date(MAX_VALIDITY_ENDS);

    private boolean isDelaying;

    /**
     * Determines, whether an action should only be repeated every 'releaseLockAfterMs' milliseconds.
     */
    private boolean isLockingAction = false;
    /**
     * Determines, that an action should only be repeated every 'releaseLockAfterMs' milliseconds.
     * If isLockingAction is 'false', this parameter has no effect.
     */
    private long releaseLockAfterMs;
    private Date startLockDate;
    private Date lockReleaseDate;

    public static final float kDefaultDistanceThreshold = Float.MAX_VALUE;
    /**
     * Determines the distance threshold. All actions whose messages were received within a lower range
     * than distanceThreshold (in meters) will be considered as 'out of range' and hence not executed.
     * Default: kDefaultDistanceThreshold
     */
    private float distanceThreshold = kDefaultDistanceThreshold;

    public BeaconAction(String actionId) {
        this.creationDate = new Date();
        this.actionId = actionId;
    }

    public void setDistanceEstimator(DistanceEstimator distanceEstimator) {
        BeaconAction.distanceEstimator = distanceEstimator;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public Date getValidityBegins() {
        return validityBegins;
    }

    public void setValidityBegins(Date postponeInSec) {
        this.validityBegins = postponeInSec;
    }

    public Date getValidityEnds() {
        return validityEnds;
    }

    public void setValidityEnds(Date validUntilInSec) {
        this.validityEnds = validUntilInSec;
    }

    /**
     * Returns true, if the action is not valid anymore.
     * @return true, when the action is expired.
     */
    public boolean isExpired() {
        Date actionValidityEnds = getValidityEnds();
        Date now = new Date();
        return now.after(actionValidityEnds);
    }

    public boolean isDelayed() {
        Date validityBegins = getValidityBegins();
        Date now = new Date();
        return now.before(validityBegins);
    }

    public void setIsDelaying(boolean isDelaying) {
        this.isDelaying = isDelaying;
    }

    public boolean isDelaying() {
        return isDelaying;
    }

    public void setLockingAction(boolean lockingAction) {
        this.isLockingAction = lockingAction;
    }

    public boolean isLockingAction() {
        return isLockingAction;
    }

    public void setReleaseLockAfterMs(long releaseLockAfterMs) {
        this.releaseLockAfterMs = releaseLockAfterMs;
    }

    public long getReleaseLockAfterMs() {
        return releaseLockAfterMs;
    }

    public void setStartLockDate(Date startLockDate) {
        this.startLockDate = startLockDate;
    }

    public Date getStartLockDate() {
        return startLockDate;
    }

    public Date getLockReleaseDate() {
        return lockReleaseDate;
    }

    public void setLockReleaseDate(Date lockReleaseDate) {
        this.lockReleaseDate = lockReleaseDate;
    }

    public boolean lockExpired() {
        Date now = new Date();
        return now.after(lockReleaseDate);
    }

    public void setSourceBeaconMessage(BeaconMessage sourceBeaconMessage) {
        this.sourceBeaconMessage = sourceBeaconMessage;
    }

    public BeaconMessage getSourceBeaconMessage() {
        return sourceBeaconMessage;
    }

    public float getDistanceThreshold() {
        return distanceThreshold;
    }

    public void setDistanceThreshold(float distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    public boolean isOutOfRange() {
        float distanceInMetres = getDistanceEstimationInMetres();
        return distanceInMetres > getDistanceThreshold();
    }

    public float getDistanceEstimationInMetres() {
        // To compare the measured RSSI based on the txPower,
        // we simply need to add the difference between the
        // calibrated and the fixed txPower. This can be shown
        // be computing a distance estimation for the RSSI
        // using the path loss formula:
        // distance = (Math.pow(10, (A-rssi)/(10*n)))
        // First compute the distance based on the calibrated txPower (A_calibrated)
        // and then recompute the RSSI using a fixed txPower (A_fixed).
        // The resulting equation can be simplified to:
        // Rssi_normalized = Rssi_measured - A_calibrated + A_fixed
        int measuredRssi = this.sourceBeaconMessage.getRssi();
        int calibratedTxPower = this.sourceBeaconMessage.getTxPower();
        float distanceInMetres = distanceEstimator.getDistanceInMetres(measuredRssi, calibratedTxPower);
        return distanceInMetres;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeaconAction)) {
            return false;
        }
        BeaconAction action = (BeaconAction) o;
        return action.getActionId().equals(this.actionId);
    }

    @Override
    public int hashCode() {
        return actionId.hashCode();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public static DistanceEstimator getDistanceEstimator() {
        return distanceEstimator;
    }

    public void execute() {
        // Default: Empty implementation
    }
}
