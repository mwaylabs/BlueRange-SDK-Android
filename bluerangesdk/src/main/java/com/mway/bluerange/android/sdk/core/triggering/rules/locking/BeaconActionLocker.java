//
//  BeaconActionLocker.java
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
