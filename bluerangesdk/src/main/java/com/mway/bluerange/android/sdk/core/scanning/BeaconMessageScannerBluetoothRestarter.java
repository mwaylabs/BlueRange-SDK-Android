//
//  BeaconMessageScannerBluetoothRestarter.java
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

package com.mway.bluerange.android.sdk.core.scanning;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import com.mway.bluerange.android.sdk.utils.logging.ITracer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BeaconMessageScannerBluetoothRestarter {

    private static final String LOG_TAG = "BluetoothRestarter";
    public static final long DEFAULT_TIME_BETWEEN_BLUETOOTH_DISABLE_AND_ENABLE_IN_MS = 100L;
    private long timeBetweenBluetoothDisableAndEnableInMs = DEFAULT_TIME_BETWEEN_BLUETOOTH_DISABLE_AND_ENABLE_IN_MS;
    public static final long DEFAULT_POLLING_TIME_TO_CHECK_STATE_CHANGE_IN_MS = 30000L;
    private long pollingTimeToCheckStateChangeInMs = DEFAULT_POLLING_TIME_TO_CHECK_STATE_CHANGE_IN_MS;
    private ITracer tracer = null;
    private Thread thread = null;
    private State state;

    private enum State {
        INITIALIZED,
        FINISHED,
        STOPPED
    }

    public BeaconMessageScannerBluetoothRestarter(ITracer tracer) {
        this.tracer = tracer;
        this.thread = null;
        this.state = State.INITIALIZED;
    }

    @TargetApi(21)
    public void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startBluetoothRestartMechanism();
                    } catch(Throwable t) {
                        // If something unexpected happened, do not
                        // quit the app. Just terminate the restart mechanism
                    }
                }
            });
            this.thread.setName("BeaconMessageScannerBluetoothRestarter");
            this.thread.start();
        }
    }

    @TargetApi(21)
    private void startBluetoothRestartMechanism() {
        tracer.logDebug(LOG_TAG, "Started Bluetooth restart mechanism.");

        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (!bluetoothScannerAvailable(bluetoothLeScanner)) {
            return;
        }

        this.state = State.INITIALIZED;
        while (this.state != State.STOPPED) {
            this.state = State.INITIALIZED;
            checkBluetoothInBackgroundAndRestartAndChangeStateWhenRestarted(bluetoothAdapter,
                    bluetoothLeScanner);
            waitUntilBluetoothRestartedOrThreadShouldBeStopped();
        }

        tracer.logDebug(LOG_TAG, "Stopped Bluetooth restart mechanism.");
    }

    private boolean bluetoothScannerAvailable(BluetoothLeScanner bluetoothLeScanner) {
        return bluetoothLeScanner != null;
    }

    @TargetApi(21)
    private BluetoothLeScanner checkBluetoothInBackgroundAndRestartAndChangeStateWhenRestarted(
            final BluetoothAdapter bluetoothAdapter, final BluetoothLeScanner bluetoothLeScanner) {
        tracer.logDebug(LOG_TAG, "Started continuously checking bluetooth.");
        bluetoothLeScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED ||
                        errorCode == SCAN_FAILED_INTERNAL_ERROR) {
                    tracer.logWarning(LOG_TAG, "Bluetooth seems to behave strange");
                    stopCheckingBluetoothInBackground(bluetoothLeScanner);
                    restartBluetoothIfStarted(bluetoothAdapter);
                }
            }
        });
        return bluetoothLeScanner;
    }

    @TargetApi(21)
    private void stopCheckingBluetoothInBackground(BluetoothLeScanner bluetoothLeScanner) {
        bluetoothLeScanner.stopScan(new ScanCallback() {
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });
    }

    private void restartBluetoothIfStarted(final BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter.isEnabled()) {
            tracer.logWarning(LOG_TAG, "Restart Bluetooth because it is enabled.");
            bluetoothAdapter.disable();
            new Timer("BeaconMessageScannerBluetoothRestarter-Timer").schedule(new TimerTask() {
                @Override
                public void run() {
                    bluetoothAdapter.enable();
                    state = State.FINISHED;
                }
            }, timeBetweenBluetoothDisableAndEnableInMs);
        } else {
            state = State.FINISHED;
        }
    }

    private void waitUntilBluetoothRestartedOrThreadShouldBeStopped() {
        while (state == State.INITIALIZED) {
            try {
                Thread.sleep(pollingTimeToCheckStateChangeInMs);
            } catch (InterruptedException e) {
                this.state = State.STOPPED;
            }
        }
    }

    public void stop() {
        this.thread.interrupt();
    }

    public long getTimeBetweenBluetoothDisableAndEnableInMs() {
        return timeBetweenBluetoothDisableAndEnableInMs;
    }

    public void setTimeBetweenBluetoothDisableAndEnableInMs(long timeBetweenBluetoothDisableAndEnableInMs) {
        this.timeBetweenBluetoothDisableAndEnableInMs = timeBetweenBluetoothDisableAndEnableInMs;
    }

    public long getPollingTimeToCheckStateChangeInMs() {
        return pollingTimeToCheckStateChangeInMs;
    }

    public void setPollingTimeToCheckStateChangeInMs(long pollingTimeToCheckStateChangeInMs) {
        this.pollingTimeToCheckStateChangeInMs = pollingTimeToCheckStateChangeInMs;
    }
}
