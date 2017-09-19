//
//  BeaconAdvertiser.java
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

package com.mway.bluerange.android.sdk.core.advertising;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;

import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;
import com.mway.bluerange.android.sdk.utils.platform.AndroidDevice;
import com.mway.bluerange.android.sdk.utils.network.Bluetooth;
import com.mway.bluerange.android.sdk.utils.string.ByteArrayConverter;

/**
 * This class can be used to send Bluetooth Low Energy advertising messages of arbitrary data. If
 * the device does not support advertising as a peripheral, a {@link
 * PeripheralAdvertisingNotSupportedException} exception is thrown when instantiating this class.
 * To use this class the device should run at least on API level 21 (Lollipop).
 */
@TargetApi(21)
public class BeaconAdvertiser {

    // Tracer
    private static final String LOG_TAG = "BeaconAdvertiser";
    private ITracer tracer = Tracer.getInstance();

    // The advertising duration.
    public static final int ADVERTISING_TIMEOUT_IN_MILLIS = 0; // infinity = 0
    // The data that will be sent in the advertising messages.
    public static final int ADVERTISING_MANUFACTURER_ID = 0x024D;
    // Data sent in the android device discovery message
    public static final String DEVICE_DISCOVERY_MESSAGE = "Android";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;

    /**
     * Creates an instance of this class
     * @param context An Android context.
     * @throws PeripheralAdvertisingNotSupportedException will be thrown, if the
     * device does not support advertising as a peripheral.
     */
    public BeaconAdvertiser(Context context) throws
            PeripheralAdvertisingNotSupportedException {
        if (!AndroidDevice.isAndroidEmulator()) {
            Bluetooth.checkBluetoothEnabled(context);
        }

        if (wrongAndroidVersion()) {
            throw new PeripheralAdvertisingNotSupportedException();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        // If the device does not support Bluetooth LE advertising, log and do nothing.
        if (!this.bluetoothAdapter.isMultipleAdvertisementSupported()) {
            printErrorMessageAdvertisingServiceNoteAvailable();
            throw new PeripheralAdvertisingNotSupportedException();
        }
    }

    private static boolean wrongAndroidVersion() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        return currentApiVersion < android.os.Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Starts advertising with {@code data} set as manufacturer specific data.
     * @param data
     */
    public void startAdvertising(byte[] data) {
        // Do nothing, if this device does not support advertising
        if (wrongAndroidVersion()) {
            return;
        }

        // Construct the advertising data and settings.
        AdvertiseSettings advertiseSettings = createAdvertiseSettingsObject();
        AdvertiseData advertiseData = createAdvertiseDataObject(data);

        // Start advertising
        this.bluetoothLeAdvertiser.startAdvertising(advertiseSettings,
                advertiseData, new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        tracer.logDebug(LOG_TAG, "Advertising started successfully!");
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        tracer.logDebug(LOG_TAG, "Advertising started with failure!");
                    }
                });
        tracer.logDebug(LOG_TAG, "BeaconAdvertiser started advertising!");
    }

    private AdvertiseSettings createAdvertiseSettingsObject() {
        AdvertiseSettings.Builder advertiseSettingsBuilder = new AdvertiseSettings.Builder();
        advertiseSettingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER);
        advertiseSettingsBuilder.setConnectable(true);
        advertiseSettingsBuilder.setTimeout(BeaconAdvertiser.ADVERTISING_TIMEOUT_IN_MILLIS);
        advertiseSettingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);
        AdvertiseSettings settings = advertiseSettingsBuilder.build();
        return settings;
    }

    private AdvertiseData createAdvertiseDataObject(byte[] data) {
        AdvertiseData.Builder advertiseDataBuilder = new AdvertiseData.Builder();
        advertiseDataBuilder.setIncludeDeviceName(false);
        advertiseDataBuilder.setIncludeTxPowerLevel(true);
        int manufacturerId = BeaconAdvertiser.ADVERTISING_MANUFACTURER_ID;
        advertiseDataBuilder.addManufacturerData(manufacturerId, data);
        AdvertiseData advertiseData = advertiseDataBuilder.build();
        return advertiseData;
    }

    /**
     * Starts advertising an Android specific advertising message that mesh beacons
     * use to distinguish iOS from Android devices.
     */
    public void startAdvertisingDiscoveryMessage() {
        byte[] data = ByteArrayConverter.stringUtf8ToByteArray(BeaconAdvertiser.DEVICE_DISCOVERY_MESSAGE);
        this.startAdvertising(data);
    }

    /**
     * Stops sending advertising messages.
     */
    public void stopAdvertising() {
        // Do nothing, if this device does not support advertising
        if (wrongAndroidVersion()) {
            return;
        }

        // If the device does not support Bluetooth LE advertising, log and do nothing.
        if (!this.bluetoothAdapter.isMultipleAdvertisementSupported()) {
            printErrorMessageAdvertisingServiceNoteAvailable();
            return;
        }
        this.bluetoothLeAdvertiser.stopAdvertising(new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                tracer.logDebug(LOG_TAG, "Advertising stopped successfully!");
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                tracer.logDebug(LOG_TAG, "Advertising stopped with failure!");
            }
        });
        tracer.logDebug(LOG_TAG, "BeaconAdvertiser stopped advertising!");
    }

    private void printErrorMessageAdvertisingServiceNoteAvailable() {
        tracer.logError(LOG_TAG, "Bluetooth LE Advertising is not supported on this device!");
    }

    public static boolean supportsAdvertising(Context context) {
        // Return false, if this device does not support advertising
        if (wrongAndroidVersion()) {
            return false;
        }

        boolean supports = false;
        try {
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            BluetoothLeAdvertiser bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            if (bluetoothAdapter.isMultipleAdvertisementSupported()) {
                supports = true;
            }
        } catch (Throwable e) {

        }
        return supports;
    }
}
