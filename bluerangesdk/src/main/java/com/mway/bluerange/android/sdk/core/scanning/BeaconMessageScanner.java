//
//  BeaconMessageScanner.java
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

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.mway.bluerange.android.sdk.common.BluetoothDisabledException;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeReceiver;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

/**
 * A beacon message scanner can be used to scan beacon messages. Before starting the scanner with
 * the {@link #startScanning} method, you must call the {@link #getConfig} method to get access
 * to the scanner's configuration. By using the configuration, you can specify which messages the
 * scanner should scan for. The {@link #startScanning} and {@link #stopScanning} methods start
 * and stop the scan procedure. If you change properties on the scanner's configuration, after
 * the scanner has been started, the scanner will automatically be restarted. To further process
 * incoming messages, register a {@link BeaconMessageStreamNodeReceiver} as a receiver.
 */
public class BeaconMessageScanner extends IBeaconMessageScanner implements BeaconConsumer {

    // Context
    private Context context;

    // Tracing
    private static final String LOG_TAG = BeaconMessageScanner.class.getSimpleName();
    private ITracer tracer = Tracer.getInstance();

    // Realization
    private BeaconManager beaconManager;

    // Configuration
    private BeaconMessageScannerConfig config;

    // Lifecycle
    private boolean meshDetected = false;
    // The time that must be elapsed, to stop advertising, when no mesh beacon has been detected.
    // This timeout has to be greater than the backgroundScanPeriodInMillis!
    public static final long DEFAULT_MESH_INACTIVE_TIMEOUT_IN_MS = 5000L;
    private long meshInactiveTimeoutInMs = DEFAULT_MESH_INACTIVE_TIMEOUT_IN_MS;
    private Timer meshActivityTimer;

    // State
    private boolean running = false;

    // Bluetooth restart mechanism
    private BeaconMessageScannerBluetoothRestarter bluetoothRestarter;

    public BeaconMessageScanner(Context context)
            throws BluetoothDisabledException {
        super();
        this.context = context;
        // Default configuration
        setConfig(new BeaconMessageScannerConfig(this));
        // Bluetooth restarter
        initBluetoothRestarter();
    }

    private void initBluetoothRestarter() {
        bluetoothRestarter = new BeaconMessageScannerBluetoothRestarter(tracer);
        // After a while some devices seem to broke Bluetooth on system level.
        // Sometimes, restarting Bluetooth helps. A similar issue and solution is
        // described in:
        // https://github.com/AltBeacon/android-beacon-library/issues/289
        // To make the scanner more robust, we continuously check
        // whether we are able to register a new BluetoothLeScanner.
        // If the scanner's registration fails, we programmatically restart Bluetooth.
        //bluetoothRestarter.start();
    }

    @Override
    public void setConfig(BeaconMessageScannerConfig config) {
        this.config = config;
    }

    @Override
    public BeaconMessageScannerConfig getConfig() {
        return this.config;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Starts a scan process that captures all beacon messages that conform to one of the beacon message
     * types specified in scanner's configuration. During the scan process all listeners
     * {@code listener} will be notified whenever new beacon messages have been scanned.
     */
    @Override
    public void startScanning() {
        // We need to synchronize the start and stop method...
        synchronized (this) {
            // Bind the service as a beacon consumer.
            this.meshDetected = false;
            this.beaconManager = BeaconManager.getInstanceForApplication(context.getApplicationContext());
            this.beaconManager.setForegroundScanPeriod(config.getScanPeriodInMillis());
            this.beaconManager.setForegroundBetweenScanPeriod(config.getBetweenScanPeriodInMillis());
            tracer.logDebug(LOG_TAG, "Started scanning!");
            this.beaconManager.bind(this);

            // Set the internal state to 'running'
            this.running = true;
        }
    }

    /**
     * Stops the scan process.
     */
    @Override
    public void stopScanning() {
        // We need to synchronize the start and stop method...
        synchronized (this) {
            stopBeaconManager();
            stopMeshActivityTimer();

            // Stop bluetooth restart thread.
            //this.bluetoothRestarter.stop();

            // Set the internal state to 'stopped'.
            this.running = false;

            tracer.logDebug(LOG_TAG, "Stopped scanning!");
        }
    }

    private void stopBeaconManager() {
        // Stop ranging
        for (BeaconMessageGenerator messageGenerator : config.getMessageGenerators()) {
            try {
                Region region = messageGenerator.getRegion();
                final String beaconLayout = messageGenerator.getBeaconLayout();
                this.beaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout(beaconLayout));
                try {
                    this.beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                tracer.logWarning(LOG_TAG, "Stopping beacon message generator "
                        + messageGenerator.getClass().getSimpleName() +
                        "failed. "  + e.getMessage() );
            }
        }
        // Unbind the beaconManager
        this.beaconManager.unbind(this);
    }

    private void stopMeshActivityTimer() {
        if (this.meshActivityTimer != null) {
            this.meshActivityTimer.cancel();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        tracer.logDebug(LOG_TAG, "ScanService bound to beacon manager.");
        // Get a callback when new beacon messages have been received.
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    BeaconMessageScanner.this.handleBeacons(beacons, region);
                }
            }
        });
        // CycledLeScannerForLollipop will not recognize beacons after some hours.
        // Therefore, we use another scanner implementation.
        // https://github.com/AltBeacon/android-beacon-library/issues/289
        // BeaconManager.setAndroidLScanningDisabled(true);
        // Start ranging beacons
        for (BeaconMessageGenerator messageGenerator : config.getMessageGenerators()) {
            try {
                Region region = messageGenerator.getRegion();
                final String beaconLayout = messageGenerator.getBeaconLayout();
                this.beaconManager.getBeaconParsers().add(new BeaconParser().
                        setBeaconLayout(beaconLayout));
                this.beaconManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception e) {
                tracer.logWarning(LOG_TAG, "Starting beacon message generator "
                        + messageGenerator.getClass().getSimpleName() +
                        " failed. "  + e.getMessage() );
            }
        }

        //this.beaconManager.setDebug(true);
        tracer.logDebug(LOG_TAG, "ScanService started ranging beacons in background!");
    }

    @Override
    public Context getApplicationContext() {
        return context.getApplicationContext();
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return this.context.bindService(intent, serviceConnection, i);
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        try {
            this.context.unbindService(serviceConnection);
        } catch(Throwable t) {
            tracer.logError(LOG_TAG, "ServiceConnection not registered.");
        }
    }

    private void handleBeacons(Collection<Beacon> beacons, Region region) {
        if (receivedAtLeastOneMeshBeacon(beacons)) {
            tracer.logDebug(LOG_TAG, "ScanService detected mesh beacons.");
            if (!meshDetected) {
                for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
                    receiver.onMeshActive(this);
                }
                this.meshDetected = true;
            }
            List<BeaconMessage> beaconMessages = createBeaconMessages(beacons, region);
            for (BeaconMessage message : beaconMessages) {
                tracer.logDebug(LOG_TAG, message.toString());
                for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
                    receiver.onReceivedMessage(this, message);
                }
            }

            this.restartMeshActivityTimer();
        }
    }

    private boolean receivedAtLeastOneMeshBeacon(Collection<Beacon> beacons) {
        if (beacons.size() > 0) {
            for (Beacon beacon : beacons) {
                if (checkIfBeaconIsMeshBeacon(beacon)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfBeaconIsMeshBeacon(Beacon beacon) {
        for (BeaconMessageGenerator messageGenerator : config.getMessageGenerators()) {
            if(messageGenerator.isValidBeacon(beacon)) {
                return true;
            }
        }
        return false;
    }

    private List<BeaconMessage> createBeaconMessages(Collection<Beacon> beacons, Region region) {
        List<BeaconMessage> beaconMessages = new ArrayList<BeaconMessage>();
        for (Beacon beacon : beacons) {
            for (BeaconMessageGenerator messageGenerator : config.getMessageGenerators()) {
                if (messageGenerator.isValidBeacon(beacon)) {
                    try {
                        BeaconMessage beaconMessage = messageGenerator.constructBeaconMessage(beacon, region);
                        beaconMessages.add(beaconMessage);
                    } catch (Exception e) {
                        tracer.logWarning(LOG_TAG, "Beacon message generator of type "
                             + messageGenerator.getClass().getSimpleName() +
                             "was not able to construct a beacon message. " +e.getMessage() );
                    }
                    break;
                }
            }
        }
        return beaconMessages;
    }

    private void restartMeshActivityTimer() {
        // Cancel the running timer if it already exists.
        if (this.meshActivityTimer != null) {
            this.meshActivityTimer.cancel();
        }
        TimerTask timerTask = new TimerTask() {
            public void run() {
                BeaconMessageScanner.this.meshInactivityTimeoutReached();
            }
        };
        // Start new timer.
        this.meshActivityTimer = new Timer("BeaconMessageScanner-MeshActivityTimer");
        this.meshActivityTimer.schedule(timerTask,
                meshInactiveTimeoutInMs);
    }

    public void meshInactivityTimeoutReached() {
        tracer.logDebug(LOG_TAG, "ScanService: Device has left mesh network or network has become inactive.");
        for (BeaconMessageStreamNodeReceiver receiver : getReceivers()) {
            receiver.onMeshInactive(this);
        }
        this.meshDetected = false;
    }

    @Override
    public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
        // Empty implementation since this class is a source node.
    }

    public long getMeshInactiveTimeoutInMs() {
        return meshInactiveTimeoutInMs;
    }

    public void setMeshInactiveTimeoutInMs(long meshInactiveTimeoutInMs) {
        this.meshInactiveTimeoutInMs = meshInactiveTimeoutInMs;
    }

    public void setTimeBetweenBluetoothDisableAndEnableInMs(long timeBetweenBluetoothDisableAndEnableInMs) {
        if (this.bluetoothRestarter != null) {
            this.bluetoothRestarter.setTimeBetweenBluetoothDisableAndEnableInMs(timeBetweenBluetoothDisableAndEnableInMs);
        }
    }

    public void setPollingTimeToCheckStateChangeInMs(long pollingTimeToCheckStateChangeInMs) {
        if (this.bluetoothRestarter != null) {
            this.bluetoothRestarter.setPollingTimeToCheckStateChangeInMs(pollingTimeToCheckStateChangeInMs);
        }
    }
}
