//
//  BeaconTrigger.java
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

package com.mway.bluerange.android.sdk.services.trigger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.mway.bluerange.android.sdk.core.aggregating.BeaconMessageAggregator;
import com.mway.bluerange.android.sdk.core.distancing.DistanceEstimator;
import com.mway.bluerange.android.sdk.core.distancing.ModelSpecificDistanceCalculator;
import com.mway.bluerange.android.sdk.core.filtering.BeaconMessageFilter;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerConfig;
import com.mway.bluerange.android.sdk.core.scanning.IBeaconMessageScanner;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconJoinMeMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeDefaultReceiver;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

import android.content.Context;

public class BeaconTrigger {

    // Tracing
    private ITracer tracer = Tracer.getInstance();

    // Message processing
    private IBeaconMessageScanner scanner;
    private BeaconMessageFilter filter;
    private BeaconMessageAggregator aggregator;

    // Filtering
    private List<Long> allowedRelutionTags;
    private List<IBeacon> allowedIBeacons;

    // Configuration
    private DistanceEstimator distanceEstimator;
    public static final int TX_POWER = -55;
    private float activationDistanceInMeter;
    private float inactivationDistanceMeter;
    private long inactivationDurationInMs;

    // Multi beacon mode. The nearest beacon is active.
    // Beacon will be activated whenever a message is received
    // with a distance difference to all other beacons of at
    // least "minDistanceDifferenceToActivate".
    private boolean multiBeaconMode = true;
    float minDistanceDifferenceToActivateInM;
    private static final long MESSAGE_OUTDATED_IN_MS = 10 * 1000;

    // State
    private Map<BeaconMessage, Boolean> activeBeacons = new HashMap<>();
    private Map<BeaconMessage, Float> distances = new HashMap<>();
    private Map<BeaconMessage, Long> timestamps = new HashMap<>();
    private Map<BeaconMessage, Timer> timers = new HashMap<>();

    // Observer
    private List<BeaconTriggerObserver> observers;

    public interface BeaconTriggerObserver {
        void onBeaconActive(BeaconMessage message);
        void onBeaconInactive(BeaconMessage message);
        void onNewDistance(BeaconMessage message, float distance);
    }

    public enum ReactionMode {
        PACKET,
        SLIDING_WINDOW
    }

    public BeaconTrigger(IBeaconMessageScanner scanner, Context context) {
        this(Tracer.getInstance(), scanner, context);
    }

    public BeaconTrigger(ITracer tracer, IBeaconMessageScanner scanner, Context context) {
        //this(tracer, scanner, new AnalyticalDistanceEstimator());
        //this(tracer, scanner, new EmpiricalDistanceEstimator());
        this(tracer, scanner, new ModelSpecificDistanceCalculator(context));
    }

    public BeaconTrigger(ITracer tracer, IBeaconMessageScanner scanner, DistanceEstimator distanceEstimator) {
        this.tracer = tracer;
        this.distanceEstimator = distanceEstimator;
        this.activationDistanceInMeter = 0.5f;
        this.inactivationDistanceMeter = 1.5f;
        // Must be high, because the beacon does not constantly send messages.
        // Sometimes we have a pause of about 4 seconds.
        this.inactivationDurationInMs = 0;
        this.minDistanceDifferenceToActivateInM = 0.6f;
        this.activeBeacons = new HashMap<>();
        this.timers = new HashMap<>();
        this.observers = new ArrayList<>();

        this.allowedRelutionTags = new ArrayList<>();
        this.allowedIBeacons = new ArrayList<>();

        this.initScanner(scanner);
        this.initAggregator();
        // Observer registration
        aggregator.addReceiver(new BeaconMessageStreamNodeDefaultReceiver() {
            @Override
            public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage
                    message) {
                onUpdateMessage(message);
            }
        });
    }

    public void stop() {
        for (Timer timer : timers.values()) {
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    private void initScanner(IBeaconMessageScanner scanner) {
        this.scanner = scanner;
    }

    private void initAggregator() {
        // Avoid premature state transitions from inactive to active
        // by using a message aggregator in sliding window mode.
        this.aggregator = new BeaconMessageAggregator(tracer, scanner);
        setReactionMode(ReactionMode.SLIDING_WINDOW);
        setReactionDurationInMs(1000L);
    }

    private void onUpdateMessage(BeaconMessage message) {
        if (message instanceof RelutionTagMessageV1) {
            RelutionTagMessageV1 relutionTagMessage = (RelutionTagMessageV1)message;
            if (relutionTagMessageContainsAtLeastOneMatchingTag(relutionTagMessage, allowedRelutionTags)) {
                updateMessage(message);
            }
        } else if (message instanceof IBeaconMessage) {
            IBeaconMessage iBeaconMessage = (IBeaconMessage) message;
            if (existsTriggeringIBeacon(iBeaconMessage, allowedIBeacons)) {
                updateMessage(message);
            }
        } else if (message instanceof BeaconJoinMeMessage) {
            /*BeaconJoinMeMessage joinMeMessage = (BeaconJoinMeMessage)message;
            if (joinMeMessage.getNodeId() == 98) {
                updateMessage(message);
            }*/
        }
    }

    private boolean relutionTagMessageContainsAtLeastOneMatchingTag(
            RelutionTagMessageV1 relutionTagMessage, List<Long> tags) {
        for (long filterTag : tags) {
            for (long messageTag : relutionTagMessage.getTags()) {
                if (filterTag == messageTag) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existsTriggeringIBeacon(
            IBeaconMessage iBeaconMessage, List<IBeacon> allowedIBeacons) {
        for (IBeacon iBeacon : allowedIBeacons) {
            IBeacon receivedIBeacon = iBeaconMessage.getIBeacon();
            if (iBeacon.equals(receivedIBeacon)) {
                return true;
            }
        }
        return false;
    }

    private void updateMessage(BeaconMessage message) {
        float distanceInMeters = estimateDistanceInMeters(message);

        // Save distance
        updateDistance(message, distanceInMeters);
        updateTimestamp(message);

        // Notify distance observers
        for (BeaconTriggerObserver observer : observers) {
            observer.onNewDistance(message, distanceInMeters);
        }

        // State changes
        //Log.d("Test", "distanceInMeters: " + distanceInMeters);
        if (distanceInMeters <= activationDistanceInMeter) {
            if (!multiBeaconMode) {
                if (isNoBeaconActive(message)) {
                    notifyObserversAboutBeaconActivation(message);
                    activateBeacon(message);
                }
            } else {
                if (beaconWinsRace(message, distanceInMeters)) {
                    notifyObserversAboutBeaconActivation(message);
                    activateBeacon(message);
                }
            }
        }
        if (!multiBeaconMode) {
            if (isBeaconActive(message) && (distanceInMeters <= inactivationDistanceMeter)) {
                refreshTimer(message);
            }
        }
    }

    private float estimateDistanceInMeters(BeaconMessage message) {
        int rssi = message.getRssi();
        int txPower = TX_POWER;
        return distanceEstimator.getDistanceInMetres(rssi, txPower);
    }

    private void updateDistance(BeaconMessage message, float distanceInMeter) {
        distances.put(message, distanceInMeter);
    }

    private void updateTimestamp(BeaconMessage message) {
        timestamps.put(message, new Date().getTime());
    }

    private boolean isNoBeaconActive(BeaconMessage message) {
        for (BeaconMessage beacon : activeBeacons.keySet()) {
            if (isBeaconActive(beacon)) {
                return false;
            }
        }
        return true;
    }

    private boolean beaconWinsRace(BeaconMessage message, float distance) {
        for (Map.Entry<BeaconMessage, Float> entry : distances.entrySet()) {
            BeaconMessage beaconMessage = entry.getKey();
            float beaconDistance = entry.getValue();
            // Do not compare with the same message
            if (!beaconMessage.equals(message)) {
                // Do not compare with messages that are "outdated".
                long nowInMs = new Date().getTime();
                long messageTimestampInMs = timestamps.get(beaconMessage);
                if (nowInMs - messageTimestampInMs < MESSAGE_OUTDATED_IN_MS) {
                    if (!(distance <= beaconDistance - minDistanceDifferenceToActivateInM)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isBeaconActive(BeaconMessage message) {
        Boolean active = activeBeacons.get(message);
        if (active != null) {
            return active;
        } else {
            return false;
        }
    }

    private void activateBeacon(BeaconMessage message) {
        activeBeacons.put(message, true);
    }

    private void notifyObserversAboutBeaconActivation(BeaconMessage message) {
        for (BeaconTriggerObserver observer : observers) {
            observer.onBeaconActive(message);
        }
    }

    private void refreshTimer(final BeaconMessage message) {
        if (inactivationDurationInMs != 0) {
            Timer timer = timers.get(message);
            if (timer != null) {
                timer.cancel();
            }
            timer = new Timer("BeaconTriggerTimer_" + message.hashCode());
            timers.put(message, timer);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    notifyObserversAboutBeaconInactivation(message);
                    inactivateBeacon(message);
                }
            }, inactivationDurationInMs);
        }
    }

    private void notifyObserversAboutBeaconInactivation(BeaconMessage message) {
        for (BeaconTriggerObserver observer : observers) {
            observer.onBeaconInactive(message);
        }
    }

    private void inactivateBeacon(BeaconMessage message) {
        activeBeacons.put(message, false);
    }

    public void addRelutionTagTrigger(long tag) {
        this.allowedRelutionTags.add(tag);
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanRelutionTagsV1(new long[]{tag});
    }

    public void addRelutionTagTriggers(long[] tags) {
        List<Long> tagList = new ArrayList<>();
        for (long tag : tags) {
            tagList.add(tag);
        }
        this.allowedRelutionTags.addAll(tagList);
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanRelutionTagsV1(tags);
    }

    public void addIBeaconTrigger(UUID uuid, int major, int minor) {
        IBeacon iBeacon = new IBeacon(uuid, major, minor);
        this.allowedIBeacons.add(iBeacon);
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanIBeacon(uuid.toString(), major, minor);
    }

    public void addIBeaconTriggers(List<IBeacon> iBeacons) {
        this.allowedIBeacons.addAll(iBeacons);
        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanIBeacons(iBeacons);
    }

    public void addObserver(BeaconTriggerObserver observer) {
        this.observers.add(observer);
    }

    public void setMultiBeaconMode(boolean multiBeaconMode) {
        this.multiBeaconMode = multiBeaconMode;
    }

    public boolean isMultiBeaconMode() {
        return multiBeaconMode;
    }

    public float getMinDistanceDifferenceToActivateInM() {
        return minDistanceDifferenceToActivateInM;
    }

    public void setMinDistanceDifferenceToActivateInM(float minDistanceDifferenceToActivateInM) {
        this.minDistanceDifferenceToActivateInM = minDistanceDifferenceToActivateInM;
    }

    // Activation distance should always be greater than inactivation distance
    public void setActivationDistance(float activationDistanceInMeter) {
        this.activationDistanceInMeter = activationDistanceInMeter;
    }

    // Activation distance should always be greater than inactivation distance
    public void setInactivationDistance(float inactivationDistanceInMeter) {
        this.inactivationDistanceMeter = inactivationDistanceInMeter;
    }

    public float getActivationDistance() {
        return activationDistanceInMeter;
    }

    public float getInactivationDistance() {
        return inactivationDistanceMeter;
    }

    public void setInactivationDurationInMs(long inactivationDurationInMs) {
        this.inactivationDurationInMs = inactivationDurationInMs;
    }

    public long getInactivationDurationInMs() {
        return inactivationDurationInMs;
    }

    public void setReactionDurationInMs(long reflectionTimeInMs) {
        this.aggregator.setAggregateDurationInMs(reflectionTimeInMs);
    }

    public long getReflectionTimeInMs() {
        return this.aggregator.getAggregateDurationInMs();
    }

    public void setReactionMode(ReactionMode reactionMode) {
        if (reactionMode == ReactionMode.SLIDING_WINDOW) {
            this.aggregator.setAggregationMode(BeaconMessageAggregator.AggregationMode.SLIDING_WINDOW);
        } else if (reactionMode == ReactionMode.PACKET) {
            this.aggregator.setAggregationMode(BeaconMessageAggregator.AggregationMode.PACEKT);
        }
    }

    public ReactionMode getReflectionMode() {
        if (this.aggregator.getAggregationMode() == BeaconMessageAggregator.AggregationMode.SLIDING_WINDOW) {
            return ReactionMode.SLIDING_WINDOW;
        } else {
            return ReactionMode.PACKET;
        }
    }
}
