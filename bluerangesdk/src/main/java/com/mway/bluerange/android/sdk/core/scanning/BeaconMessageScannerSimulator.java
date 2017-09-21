//
//  BeaconMessageScannerSimulator.java
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

package com.mway.bluerange.android.sdk.core.scanning;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconJoinMeMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.EddystoneUidMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.EddystoneUrlMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

/**
 * This class implements the
 * {@link IBeaconMessageScanner} interface and can be used to simulate incoming {@link BeaconMessage}s.
 */
public class BeaconMessageScannerSimulator extends IBeaconMessageScanner {

    // Tracer
    private ITracer tracer = Tracer.getInstance();
    private static final String LOG_TAG = "BeaconMessageScannerSimulator";

    private List<BeaconMessage> beaconMessages = new ArrayList<>();
    private boolean repeat = false;
    private long repeatInterval;
    private BeaconMessageScannerConfig config;

    // Thread
    private Thread thread;

    // Internal state
    private boolean running = false;
    private boolean rssiNoise = false;

    public BeaconMessageScannerSimulator() {
        super();
        config = new BeaconMessageScannerConfig(this);
    }

    public void addRssiNoise() {
        this.rssiNoise = true;
    }

    public void simulateIBeacon(String uuid, int major, int minor) {
        this.beaconMessages.add(new IBeaconMessage(UUID.fromString(uuid), major, minor));
    }

    public void simulateIBeacon(IBeacon iBeacon) {
        simulateIBeacon(iBeacon.getUuid().toString(), iBeacon.getMajor(), iBeacon.getMinor());
    }

    private static class IBeaconMessageFake extends IBeaconMessage {
        public IBeaconMessageFake(UUID uuid, int major, int minor) {
            super(uuid, major, minor);
        }
    }

    public void simulateIBeaconWithRssi(String uuid, int major, int minor, final int rssi) {
        // Fake rssi of iBeacon
        IBeaconMessageFake iBeaconMessage = new IBeaconMessageFake(UUID.fromString(uuid), major, minor);
        iBeaconMessage.setRssi(rssi);
        this.beaconMessages.add(iBeaconMessage);
    }

    public void simulateEddystoneUid(String namespaceUid, String instance) {
        this.beaconMessages.add(new EddystoneUidMessage(namespaceUid, instance));
    }

    public void simulateEddystoneUidWithRssi(String namespaceUid, String instance, int rssi) {
        EddystoneUidMessage message = new EddystoneUidMessage(namespaceUid, instance);
        message.setRssi(rssi);
        this.beaconMessages.add(message);
    }

    public void simulateEddystoneUrl(String url) {
        this.beaconMessages.add(new EddystoneUrlMessage(url));
    }

    public void simulateEddystoneUrlWithRssi(String url, int rssi) {
        EddystoneUrlMessage message = new EddystoneUrlMessage(url);
        message.setRssi(rssi);
        this.beaconMessages.add(message);
    }

    public void simulateRelutionTagsV1(long[] tags) {
        this.beaconMessages.add(new RelutionTagMessageV1(tags));
    }

    private static class RelutionTagMessageFake extends RelutionTagMessageV1 {
        public RelutionTagMessageFake(long[] tags) {
            super(tags);
        }
    }

    public void simulateRelutionTagsV1WithRssi(long[] tags, final int rssi) {
        RelutionTagMessageFake message = new RelutionTagMessageFake(tags);
        message.setRssi(rssi);
        this.beaconMessages.add(message);
    }

    public void simulateRelutionTags(String namespaceUid, int[] tags) {
        try {
            RelutionTagMessage message = new RelutionTagMessage(namespaceUid, tags);
            this.beaconMessages.add(message);
        } catch (Exception e) {
            tracer.logWarning(LOG_TAG, "Adding Relution Tag message " +
                    "to simulator failed. " + e.getMessage());
        }
    }

    public void simulateRelutionTagsWithRssi(String namespaceUid, int[] tags, final int rssi) {
        try {
            RelutionTagMessage message = new RelutionTagMessage(namespaceUid, tags);
            message.setRssi(rssi);
            this.beaconMessages.add(message);
        } catch (Exception e) {
            tracer.logWarning(LOG_TAG, "Adding Relution Tag message " +
                    "to simulator failed. " + e.getMessage());
        }
    }

    private static class BeaconJoinMeMessageFake extends BeaconJoinMeMessage {
        public BeaconJoinMeMessageFake(Date date, int sender, long clusterId,
                                       short clusterSize, short freeInConnections,
                                       short freeOutConnections, short batteryRuntime,
                                       short txPower, short deviceType, int hopsToSink,
                                       int meshWriteHandle, int ackField) {
            super(date, sender, clusterId, clusterSize, freeInConnections, freeOutConnections,
                    batteryRuntime, txPower, deviceType, hopsToSink, meshWriteHandle, ackField);
        }
    }

    public void simulateJoinMe(final int nodeId, final int rssi) {
        Date date = new Date();
        int sender = nodeId;
        long clusterId = 0;
        short clusterSize = 0;
        short freeInConnections = 0;
        short freeOutConnections = 0;
        short batteryRuntime = 0;
        short txPower = 0;
        short deviceType = 0;
        int hopsToSink = 0;
        int meshWriteHandle = 0;
        int ackField = 0;
        BeaconJoinMeMessageFake message = new BeaconJoinMeMessageFake(date, sender, clusterId, clusterSize,
                freeInConnections, freeOutConnections, batteryRuntime, txPower,
                deviceType, hopsToSink, meshWriteHandle, ackField);
        message.setRssi(rssi);
        this.beaconMessages.add(message);
    }

    public void resetSimulatedBeacons() {
        this.beaconMessages.clear();
    }

    @Override
    public void setConfig(BeaconMessageScannerConfig config) {
        // We do not need to configure anything.
        this.config = config;
    }

    @Override
    public BeaconMessageScannerConfig getConfig() {
        return config;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void startScanning() {
        if (repeat) {
            startRepeatScan();
        } else {
            startOneTimeScan();
        }
        this.running = true;
    }

    private void startRepeatScan() {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        startOneTimeScan();
                        Thread.sleep(repeatInterval);
                    }
                } catch (InterruptedException e) {
                    // An interrupt stops the thread.
                }
            }
        });
        this.thread.setName("BeaconMessageScannerSimulator");
        this.thread.start();
    }

    private void startOneTimeScan() {
        // Clone messages
        List<BeaconMessage> clonedMessages = getClonedMessages();
        for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
            for(BeaconMessage beaconMessage : clonedMessages) {
                if (rssiNoise) {
                    int rssi = beaconMessage.getRssi();
                    float noiseStrength = 10;
                    float noise = (float)((Math.random() * noiseStrength) - noiseStrength/2);
                    float noisedRssi = rssi + noise;
                    //Log.d("Rssi", "rssi = " + rssi);
                    //Log.d("Rssi", "noised rssi = " + noisedRssi);
                    beaconMessage.setRssi((int)noisedRssi);
                }
                beaconMessage.setTimestamp(new Date());
                receiver.onReceivedMessage(this, beaconMessage);
            }
        }
    }

    private List<BeaconMessage> getClonedMessages() {
        List<BeaconMessage> clonedMessages = new ArrayList<>();
        for(BeaconMessage beaconMessage : beaconMessages) {
            clonedMessages.add(beaconMessage.clone());
        }
        return clonedMessages;
    }

    public void restartSimulator() {
        this.stopScanning();
        this.startScanning();
    }

    @Override
    public void stopScanning() {
        this.running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        // Do not do anything
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }
}
