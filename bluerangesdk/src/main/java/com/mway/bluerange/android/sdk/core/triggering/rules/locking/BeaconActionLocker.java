//
//  BeaconActionLocker.java
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

package com.mway.bluerange.android.sdk.core.triggering.rules.locking;

import com.mway.bluerange.android.sdk.core.triggering.BeaconAction;
import com.mway.bluerange.android.sdk.core.triggering.rules.RunningFlag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

public class BeaconActionLocker extends Thread {

    public static final long POLLING_TIME_FOR_CHECKING_LOCKS_IN_MS = 500l;
    private long pollingTimeForCheckingLocksInMs = POLLING_TIME_FOR_CHECKING_LOCKS_IN_MS;
    private final List<BeaconAction> actionLocks = new ArrayList<BeaconAction>();
    private final RunningFlag runningFlag;

    public BeaconActionLocker(RunningFlag runningFlag) {
        this.runningFlag = runningFlag;
    }

    public boolean actionIsCurrentlyLocked(BeaconAction action) {
        // If at least one action exists, that is locked and has the same id,
        // the passed action is considered to be locked.
        synchronized(actionLocks) {
            for (BeaconAction actionLock : actionLocks) {
                BeaconAction referenceAction = actionLock;
                if (action.getActionId().equals(referenceAction.getActionId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void rememberLock(BeaconAction action) {
        // If at least one action exists, that is locked and has the same id,
        // the passed action is considered to be locked.
        synchronized(actionLocks) {
            for (BeaconAction actionLock : actionLocks) {
                BeaconAction referenceAction = actionLock;
                if (action.getActionId().equals(referenceAction.getActionId())) {
                    action.setStartLockDate(actionLock.getStartLockDate());
                    action.setLockReleaseDate(actionLock.getLockReleaseDate());
                }
            }
        }
    }

    public void addActionLock(BeaconAction action) {
        // To lock an action, remember the release date
        // and save the action to the actionLocks list.
        synchronized (actionLocks) {
            // Remember the release date.
            Date now = new Date();
            long nowInMs = now.getTime();
            long lockDurationInMs = action.getReleaseLockAfterMs();
            long releaseLockDateInMs = nowInMs + lockDurationInMs;
            Date releaseLockDate = new Date(releaseLockDateInMs);
            action.setStartLockDate(new Date());
            action.setLockReleaseDate(releaseLockDate);
            // Add it to the list.
            actionLocks.add(action);
        }
    }

    @Override
    public void run() {
        constantlyRemoveExpiredLocks();
    }

    private void constantlyRemoveExpiredLocks() {
        try {
            while(runningFlag.isRunning()) {
                // 1. Release expired locks.
                releaseExpiredLocks();
                // 2. Sleep a while
                waitAWhile();
            }
        } catch (InterruptedException e) {
            // If interrupt is thrown, terminate this thread instantly.
        }
    }

    private void releaseExpiredLocks() {
        // Check all action locks, whether they should be released.
        // Release them, if this is the case.
        synchronized (actionLocks) {
            // We use the list iterator to be able to remove items while iterating.
            ListIterator<BeaconAction> iterator = actionLocks.listIterator();
            while (iterator.hasNext()) {
                BeaconAction actionLock = iterator.next();
                if (actionLock.lockExpired()) {
                    iterator.remove();
                }
            }
        }
    }

    private void waitAWhile() throws InterruptedException {
        Thread.sleep(pollingTimeForCheckingLocksInMs);
    }

    public long getPollingTimeForCheckingLocksInMs() {
        return pollingTimeForCheckingLocksInMs;
    }

    public void setPollingTimeForCheckingLocksInMs(long pollingTimeForCheckingLocksInMs) {
        this.pollingTimeForCheckingLocksInMs = pollingTimeForCheckingLocksInMs;
    }
}
