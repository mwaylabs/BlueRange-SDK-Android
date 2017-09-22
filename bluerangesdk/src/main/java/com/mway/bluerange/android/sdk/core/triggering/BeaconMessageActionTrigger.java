//
//  BeaconMessageActionTrigger.java
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

package com.mway.bluerange.android.sdk.core.triggering;

import com.mway.bluerange.android.sdk.core.aggregating.BeaconMessageAggregator;
import com.mway.bluerange.android.sdk.core.distancing.AnalyticalDistanceEstimator;
import com.mway.bluerange.android.sdk.core.distancing.DistanceEstimator;
import com.mway.bluerange.android.sdk.core.filtering.IBeaconMessageFilter;
import com.mway.bluerange.android.sdk.core.filtering.RelutionTagMessageFilter;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessagePassingStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageQueuedStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;

import com.mway.bluerange.android.sdk.core.triggering.rules.RunningFlag;
import com.mway.bluerange.android.sdk.core.triggering.rules.locking.BeaconActionLocker;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * A trigger instance is a node in a message processing graph that is able to trigger actions,
 * whenever messages will be received, that an action registry is able to map to an action.<br>
 *     Before an action will be triggered, the message stream is filtered, so that only iBeacon
 *     and Relution Tag messages will be considered in the further steps. To stabilize the RSSI
 *     values of the incoming messages, a message aggregator aggregates equivalent messages and
 *     averages the RSSI values by using a linearly weighted moving average filter. The resulting
 *     stream of aggregated messages is then delivered to a message queue, which the trigger
 *     iteratively pulls messages out of. Each message is then mapped to an action using the
 *     action registry, which can e.g. call a remote webservice. If the registry is not currently
 *     available, the trigger mechanism waits until the registry has become available. In this
 *     time the message queue will in most cases accumulate a lot of messages. Since the queue,
 *     however, has a limited size, these situations will not result in a memory leak. The
 *     advantage of this strategy, however, is, that actions can be executed at a later time, e.g
 *     . when internet has become available.<br> Before an action will be executed, it has to
 *     pass a sequence of checks, since actions can be equipped with different time and location
 *     based parameters. One of these parameters is a distance threshold. The action executor
 *     first transforms the RSSI value of the action initiating message to a distance value and
 *     then checks whether this value is below a distance threshold being defined in the action's
 *     description. If this is not the case, the action will be discarded. In the other case the
 *     executor checks, whether the action validation period is expired. An expiration will also
 *     result in an action discard. Another situation, when an action will be discarded, occurs,
 *     when an equivalent action has set a lock to this action for a specific duration. As long
 *     as the lock is set, no actions with the same action ID will be executed. If the action
 *     should be executed with a delay, it will be added to an action delay queue and executed
 *     when the delay time has elapsed.
 */
public class BeaconMessageActionTrigger extends BeaconMessagePassingStreamNode implements Runnable {

    // Tracer
    private ITracer tracer;
    private static final String LOG_TAG = "Trigger";

    // Debugging
    private boolean debugModeOn = false;
    private List<BeaconActionDebugListener> debugActionListeners = new ArrayList<>();

    // Sender nodes
    private BeaconMessageAggregator aggregator;
    private BeaconMessageQueuedStreamNode queueNode;
    // Queue should not exceed 10 thousand messages.
    public static final int DEFAULT_MAXIMUM_QUEUE_SIZE = 10 * 1024;

    // Message processing
    private RunningFlag running;
    private Thread messageProcessingThread;

    // Action registry
    private BeaconActionRegistry actionRegistry;
    public static final long DEFAULT_POLLING_TIME_FOR_CHECKING_REGISTRY_AVAILABLE_IN_MS = 60000l;
    private long pollingTimeForCheckingRegistryAvailable
            = DEFAULT_POLLING_TIME_FOR_CHECKING_REGISTRY_AVAILABLE_IN_MS;

    // Action range checker
    private DistanceEstimator distanceEstimator;

    // Action execution delayer
    private final List<BeaconAction> delayedActions = new ArrayList<>();
    private Thread delayedActionExecutionThread;
    public static final long DEFAULT_POLLING_TIME_FOR_CHECKING_DELAYED_ACTIONS_IN_MS = 1000l;

    private long pollingTimeForCheckingDelayedActionsInMs
            = DEFAULT_POLLING_TIME_FOR_CHECKING_DELAYED_ACTIONS_IN_MS;

    // Action locker
    private BeaconActionLocker actionLocker;

    // Action execution
    private List<BeaconActionListener> listeners = new ArrayList<>();

    public BeaconMessageActionTrigger(BeaconMessageStreamNode senderNode,
                                      BeaconActionRegistry actionRegistry) {
        this(Tracer.getInstance(),
                senderNode, actionRegistry,
                new AnalyticalDistanceEstimator());
    }

    public BeaconMessageActionTrigger(ITracer tracer, BeaconMessageStreamNode senderNode,
                                      BeaconActionRegistry actionRegistry,
                                      DistanceEstimator distanceEstimator) {
        // Save tracer
        this.tracer = tracer;

        // In order to decouple the sender message processing from the message processing
        // of the trigger, we use a message queue, from which we can pull beacon messages
        // asynchronously. Right before queueing the message, we use a filter to only
        // process iBeacon and Relution tag messages. The stream of iBeacon and Relution
        // tag messages will then be transformed to dense packets of iBeacon and Relution tag
        // messages, so that actions will only be triggered, when the same message is
        // received multiple times in a small amount of time.
        IBeaconMessageFilter iBeaconMessageFilter = new IBeaconMessageFilter(senderNode);
        RelutionTagMessageFilter relutionTagMessageFilter = new RelutionTagMessageFilter(senderNode);
        List<BeaconMessageStreamNode> filters = new ArrayList<>();
        filters.add(iBeaconMessageFilter);
        filters.add(relutionTagMessageFilter);
        this.aggregator = new BeaconMessageAggregator(tracer, filters);
        this.queueNode = new BeaconMessageQueuedStreamNode(filters);
        this.queueNode.setMaximumSize(DEFAULT_MAXIMUM_QUEUE_SIZE);
        this.addSender(queueNode);

        // Initialize the actionRegistry.
        this.actionRegistry = actionRegistry;

        // Define the internal state
        this.running = new RunningFlag(false);

        // Message processor
        this.messageProcessingThread = null;

        // Action range checker
        this.distanceEstimator = distanceEstimator;

        // Action Locker
        this.actionLocker = new BeaconActionLocker(running);
    }

    public void start() {
        this.startMessageProcessingThread();
        this.startDelayedActionExecutionThread();
    }

    private void startMessageProcessingThread() {
        this.running.setRunning(true);
        this.messageProcessingThread = new Thread(this);
        this.messageProcessingThread.setName("BeaconMessageActionTrigger");
        this.messageProcessingThread.start();
        this.actionLocker.start();
    }

    @Override
    public void run() {
        try {
            while (running.isRunning()) {
                // 1. Pull the next message from the message queue.
                BeaconMessage message = queueNode.pullBeaconMessage();
                try {
                    // 2. Wait until the action registry is available for this message.
                    waitUntilActionRegistryIsAvailableForMessage(message);
                    // 3. Get beacon actions from action registry.
                    List<BeaconAction> actions = actionRegistry.getBeaconActionsForMessage(message);
                    // 4. Configure actions
                    configureActions(actions);
                    // 5. Execute the actions if not expired
                    executeActions(actions);
                } catch (BeaconActionRegistry.UnsupportedMessageException e) {
                    // We just skip messages that cannot be mapped to actions.
                    tracer.logWarning(LOG_TAG, "Skipped action, because message is not supported!");
                } catch (BeaconActionRegistry.RegistryNotAvailableException e) {
                    // If the registry is not available the triggering
                    // mechanism should not lead to an overflowing message queue.
                    // Therefore, we discard these messages.
                    tracer.logWarning(LOG_TAG, "Skipped action, because registry is currently not available!");
                } catch (Throwable throwable) {
                    // Log the unexpected exception and continue with the next action.
                    if (!(throwable instanceof InterruptedException)) {
                        tracer.logError(LOG_TAG, "Unexpected action!");
                    } else {
                        // Rethrow the interrupted exception.
                        throw throwable;
                    }
                }
            }
        } catch (InterruptedException e) {
            // If an interrupt is thrown when we wait for the next
            // beacon message, we just stop the triggering thread.
        }
    }

    private void waitUntilActionRegistryIsAvailableForMessage(BeaconMessage message)
            throws InterruptedException, BeaconActionRegistry.UnsupportedMessageException {
        while (!actionRegistry.isAvailable(message)) {
            Thread.sleep(pollingTimeForCheckingRegistryAvailable);
            tracer.logDebug(LOG_TAG, "Waiting for action registry to become available.");
        }
    }

    private void configureActions(List<BeaconAction> actions) {
        for (BeaconAction action : actions) {
            action.setDistanceEstimator(distanceEstimator);
        }
    }

    private void executeActions(List<BeaconAction> actions) {
        for (BeaconAction action : actions) {
            // Debug listeners can inspect and supervise the action.
            notifyDebugListeners(action);
            // 0. Ignore all actions that are out of range
            if (!action.isOutOfRange()) {
                // 2. Ignore all actions that are expired.
                if (!action.isExpired()) {
                    // 3. Ignore all actions that are currently locked.
                    if (!actionLocker.actionIsCurrentlyLocked(action)) {
                        // 4.1 Lock action if lock is enabled
                        if (action.isLockingAction()) {
                            actionLocker.addActionLock(action);
                        }
                        // 4.2 Check if action is not delayed
                        if (!action.isDelayed()) {
                            executeAndNotifyListeners(action);
                        } else {
                            // If action is delayed, execute it later.
                            addActionToDelayedActionQueue(action);
                            tracer.logDebug(LOG_TAG, "Added action to delay queue: " + action.getClass().getSimpleName());
                        }
                    } else {
                        actionLocker.rememberLock(action);
                        tracer.logDebug(LOG_TAG, "Action is locked: " + action.getClass().getSimpleName());
                    }
                } else {
                    tracer.logDebug(LOG_TAG, "Action is expired: " + action.getClass().getSimpleName());
                }
            } else {
                tracer.logDebug(LOG_TAG, "Action is out of range: " + action.getClass().getSimpleName());
                tracer.logDebug(LOG_TAG, "Distance threshold: " + action.getDistanceThreshold() + " meters");
                tracer.logDebug(LOG_TAG, "Estimated distance: " + action.getDistanceEstimationInMetres() + " meters");
            }
        }
    }

    private void executeAndNotifyListeners(BeaconAction action) {
        action.execute();
        notifyListeners(action);
    }

    private void notifyListeners(BeaconAction action) {
        for (BeaconActionListener listener : listeners) {
            listener.onActionTriggered(action);
        }
    }

    private void notifyDebugListeners(BeaconAction action) {
        for (BeaconActionDebugListener listener : debugActionListeners) {
            listener.onActionExecutionStarted(action);
        }
    }

    private void addActionToDelayedActionQueue(BeaconAction action) {
        synchronized (delayedActions) {
            delayedActions.add(action);
            action.setIsDelaying(true);
        }
    }

    private void startDelayedActionExecutionThread() {
        this.delayedActionExecutionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (running.isRunning()) {
                        // 1. Execute elapsed actions
                        executeElapsedActions();
                        // 2. Wait a while
                        waitAWhile();
                    }
                } catch (InterruptedException e) {
                    // When interrupt was called, do not process
                    // any delayed actions.
                }
            }

            private void executeElapsedActions() {
                // Save modification of delayedActions by synchronizing the threads
                // accessing the list.
                synchronized (delayedActions) {
                    // We use a ListIterator to be able to remove actions while iterating.
                    ListIterator<BeaconAction> iterator = delayedActions.listIterator();
                    while (iterator.hasNext()) {
                        BeaconAction delayedAction = iterator.next();
                        // If action is not delayed anymore, we execute the action and
                        // remove it from the list.
                        if (!delayedAction.isDelayed()) {
                            // Execute the action.
                            executeAndNotifyListeners(delayedAction);
                            // Remove the action from the list.
                            iterator.remove();
                        }
                    }
                }
            }

            private void waitAWhile() throws InterruptedException {
                Thread.sleep(pollingTimeForCheckingDelayedActionsInMs);
            }

        });
        this.delayedActionExecutionThread.setName("Delayed Action Execution Thread");
        this.delayedActionExecutionThread.start();
    }

    public void stop() {
        this.stopThread();
    }

    private void stopThread() {
        this.running.setRunning(false);
        this.messageProcessingThread.interrupt();
        this.delayedActionExecutionThread.interrupt();
        this.actionLocker.interrupt();
        if (aggregator != null) {
            aggregator.stop();
        }
    }

    // Action execution

    public void addActionListener(BeaconActionListener listener) {
        listeners.add(listener);
    }

    public void removeActionListener(BeaconActionListener listener) {
        listeners.remove(listener);
    }

    // Debugging

    public void addDebugActionListener(BeaconActionDebugListener listener) {
        this.debugActionListeners.add(listener);
    }

    public boolean isDebugModeOn() {
        return debugModeOn;
    }

    public void setDebugModeOn(boolean debugModeOn) {
        this.debugModeOn = debugModeOn;
    }

    // Distancing

    public DistanceEstimator getDistanceEstimator() {
        return distanceEstimator;
    }

    public void setDistanceEstimator(DistanceEstimator distanceEstimator) {
        this.distanceEstimator = distanceEstimator;
    }

    // Aggregator
    public BeaconMessageAggregator getAggregator() {
        return aggregator;
    }

    // Queue
    public void setMaximumQueueSize(int maximumQueueSize) {
        this.queueNode.setMaximumSize(maximumQueueSize);
    }

    public int getMaximumQueueSize() {
        return this.queueNode.getMaximumSize();
    }

    public long getPollingTimeForCheckingRegistryAvailable() {
        return pollingTimeForCheckingRegistryAvailable;
    }

    public void setPollingTimeForCheckingRegistryAvailable(long pollingTimeForCheckingRegistryAvailable) {
        this.pollingTimeForCheckingRegistryAvailable = pollingTimeForCheckingRegistryAvailable;
    }

    public long getPollingTimeForCheckingDelayedActionsInMs() {
        return pollingTimeForCheckingDelayedActionsInMs;
    }

    public void setPollingTimeForCheckingDelayedActionsInMs(long pollingTimeForCheckingDelayedActionsInMs) {
        this.pollingTimeForCheckingDelayedActionsInMs = pollingTimeForCheckingDelayedActionsInMs;
    }

    public long getPollingTimeForCheckingLocksInMs() {
        return this.actionLocker.getPollingTimeForCheckingLocksInMs();
    }

    public void setPollingTimeForCheckingLocksInMs(long pollingTimeForCheckingLocksInMs) {
        this.actionLocker.setPollingTimeForCheckingLocksInMs(pollingTimeForCheckingLocksInMs);
    }

    public void setAggregateDurationInMs(long aggregateDurationInMs) {
        if (this.aggregator != null) {
            this.aggregator.setAggregateDurationInMs(aggregateDurationInMs);
        }
    }
}
