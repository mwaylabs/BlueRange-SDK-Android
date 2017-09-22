//
//  BeaconMessageScannerConfig.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mway.bluerange.android.sdk.core.scanning.messages.AssetTrackingMessageV1Generator;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconJoinMeMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.EddystoneMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.EddystoneUidMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.EddystoneUrlMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageGenerator;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageGeneratorV1;

/**
 * This class encapsulates the {@link BeaconMessageScanner}'s configuration. If the configuration
 * is changed, while the scanner is running, the scanner will be restarted automatically with the
 * updated configuration. To add messages the scanner should scan for, call the methods that
 * start with 'scan'. The scanPeriod defines the interval duration of each scan cycle. The lower
 * this value is, the more messages can be processed in a certain time. However setting this
 * value too low, might decrease the overall performance of your application, since all
 * components of the message processing architecture, might depend on the duration of a scan
 * cycle. Moreover, you should consider, that a low value will increase the battery consumption
 * due to the increased CPU utilization.
 */
public class BeaconMessageScannerConfig implements Serializable {

    // Back reference to the scanner
    private IBeaconMessageScanner scanner;
    // The message types the scanner should scan for.
    private List<BeaconMessageGenerator> messageGenerators = new ArrayList<BeaconMessageGenerator>();
    // The periodic time that is spent collecting beacon messages until they are inspected.
    private long scanPeriodInMillis = 500L;//10l;
    // The time between the scan periods where no beacons will be detected.
    private long betweenScanPeriodInMillis = 1L;

    public BeaconMessageScannerConfig(IBeaconMessageScanner scanner) {
        this.scanner = scanner;
    }

    private void addMessageGeneratorAndRestartScannerIfNecessary(BeaconMessageGenerator messageGenerator) {
        boolean restart = scanner.isRunning();
        stopScannerIfIsRunning(restart);
        addMessageGenerator(messageGenerator);
        startScannerIfDidRunBefore(restart);
    }

    private void addMessageGeneratorsAndRestartScannerIfNecessary(List<BeaconMessageGenerator> messageGenerators) {
        // Restart the scanner only once with the new configuration
        if (messageGenerators.size() > 0) {
            boolean restart = scanner.isRunning();
            stopScannerIfIsRunning(restart);
            for (int i = 0; i < messageGenerators.size(); i++) {
                BeaconMessageGenerator messageGenerator = messageGenerators.get(i);
                addMessageGenerator(messageGenerator);
            }
            startScannerIfDidRunBefore(restart);
        }
    }

    private void addMessageGenerator(BeaconMessageGenerator messageGenerator) {
        this.messageGenerators.add(messageGenerator);
        // Ensure that message generators do not overlap.
        ensureMessageGeneratorDisjunction();
    }

    private void ensureMessageGeneratorDisjunction() {
        // Since Relution Tag messages can be parsed as Eddystone UID messages as well,
        // we have to explicitly blacklist them from the Eddystone messages.
        List<RelutionTagMessageGenerator> relutionTagMessageGenerators = getRelutionTagMessageGenerators();
        List<EddystoneMessageGenerator> eddystoneMessageGenerators = getEddystoneMessageGenerators();
        for (RelutionTagMessageGenerator relutionTagMessageGenerator : relutionTagMessageGenerators) {
            String namespaceUid = relutionTagMessageGenerator.getNamespace();
            for (EddystoneMessageGenerator eddystoneMessageGenerator : eddystoneMessageGenerators) {
                if (!(eddystoneMessageGenerator instanceof RelutionTagMessageGenerator)) {
                    blacklistNamespaceInEddystoneMessageGeneratorIfMatches(
                            eddystoneMessageGenerator, namespaceUid);
                }
            }
        }
    }

    private List<RelutionTagMessageGenerator> getRelutionTagMessageGenerators() {
        List<RelutionTagMessageGenerator> relutionTagMessageGenerators = new ArrayList<>();
        for (BeaconMessageGenerator messageGenerator : messageGenerators) {
            if (messageGenerator instanceof RelutionTagMessageGenerator) {
                relutionTagMessageGenerators.add((RelutionTagMessageGenerator) messageGenerator);
            }
        }
        return relutionTagMessageGenerators;
    }

    private List<EddystoneMessageGenerator> getEddystoneMessageGenerators() {
        List<EddystoneMessageGenerator> eddystoneMessageGenerators = new ArrayList<>();
        for (BeaconMessageGenerator messageGenerator : messageGenerators) {
            if (messageGenerator instanceof EddystoneMessageGenerator) {
                eddystoneMessageGenerators.add((EddystoneMessageGenerator) messageGenerator);
            }
        }
        return eddystoneMessageGenerators;
    }

    private void blacklistNamespaceInEddystoneMessageGeneratorIfMatches(
            EddystoneMessageGenerator eddystoneMessageGenerator, String namespaceUid) {
        if (eddystoneMessageGenerator instanceof EddystoneUidMessageGenerator) {
            EddystoneUidMessageGenerator eddystoneUidMessageGenerator =
                    (EddystoneUidMessageGenerator) eddystoneMessageGenerator;
            String comparedNamespace = eddystoneUidMessageGenerator.getNamespace();
            if (comparedNamespace == null || comparedNamespace.equals(namespaceUid)) {
                eddystoneUidMessageGenerator.blacklistNamespace(namespaceUid);
            }
        } else if (eddystoneMessageGenerator instanceof EddystoneUrlMessageGenerator) {

        }
    }

    /**
     * Adds an iBeacon with the {@code uuid}, {@code major} and {@code minor}
     * identifiers to the list of iBeacons that should be scanned for.
     * @param uuid The iBeacon's UUID (e.g. "D9B9EC1F-3925-43D0-80A9-1E39D4CEA95C")
     * @param major The iBeacon's Major identifier
     * @param minor The iBeacon's Minor identifier.
     */
    public synchronized void scanIBeacon(String uuid, int major, int minor) {
        IBeaconMessageGenerator messageGenerator = new IBeaconMessageGenerator(uuid, major, minor);
        // If the same message generator already exists, do not add it twice.
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Enables scanning of all iBeacons with the UUID {@code uuid} and major {@code major}
     * @param uuid The iBeacon UUID that should be scanned for.
     * @param major The iBeacon 'major' identifier that should be scanned for.
     */
    public synchronized void scanIBeacon(String uuid, int major) {
        IBeaconMessageGenerator messageGenerator = new IBeaconMessageGenerator(uuid, major);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Enables scanning of all iBeacons with the UUID {@code uuid}
     * @param uuid The iBeacon UUID that should be scanned for.
     */
    public synchronized void scanIBeacon(String uuid) {
        IBeaconMessageGenerator messageGenerator = new IBeaconMessageGenerator(uuid);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    public synchronized void scanIBeaconUuids(List<String> uuids) {
        List<BeaconMessageGenerator> newMessageGenerators = new ArrayList<>();
        // Add all message generators to the list if it does not exist yet in the list of all message generators.
        for (int i = 0; i < uuids.size(); i++) {
            String uuid = uuids.get(i);
            IBeaconMessageGenerator messageGenerator = new IBeaconMessageGenerator(uuid);
            if (!messageGenerators.contains(messageGenerator)) {
                newMessageGenerators.add(messageGenerator);
            }
        }
        addMessageGeneratorsAndRestartScannerIfNecessary(newMessageGenerators);
    }

    public synchronized void scanIBeacons(List<IBeacon> iBeacons) {
        List<BeaconMessageGenerator> newMessageGenerators = new ArrayList<>();
        // Add all message generators to the list if it does not exist yet in the list of all message generators.
        for (int i = 0; i < iBeacons.size(); i++) {
            IBeacon iBeacon = iBeacons.get(i);
            String uuid = iBeacon.getUuid().toString();
            int major = iBeacon.getMajor();
            int minor = iBeacon.getMinor();

            IBeaconMessageGenerator messageGenerator = new IBeaconMessageGenerator(uuid, major, minor);
            if (!messageGenerators.contains(messageGenerator)) {
                newMessageGenerators.add(messageGenerator);
            }
        }
        addMessageGeneratorsAndRestartScannerIfNecessary(newMessageGenerators);
    }

    /**
     * Enables scanning of all Relution tag messages.
     */
    public synchronized void scanRelutionTagsV1() {
        RelutionTagMessageGeneratorV1 messageGenerator = new RelutionTagMessageGeneratorV1();
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Adds the relution tags {@code tags} to the list Relution tags that should be scanned for.
     * @param tags The list of the 24 bit Relution tags.
     */
    public synchronized void scanRelutionTagsV1(long[] tags) {
        RelutionTagMessageGeneratorV1 messageGenerator = new RelutionTagMessageGeneratorV1(tags);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    public void scanRelutionTags(String namespaceUid) {
        RelutionTagMessageGenerator messageGenerator = new RelutionTagMessageGenerator(namespaceUid);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    public void scanRelutionTags(String namespaceUid, int[] tags) throws Exception {
        RelutionTagMessageGenerator messageGenerator = new RelutionTagMessageGenerator(namespaceUid, tags);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Configures the scanner to scan all Eddystone UID messages.
     */
    public synchronized void scanEddystoneUid() {
        EddystoneUidMessageGenerator messageGenerator = new EddystoneUidMessageGenerator();
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Configures the scanner to scan all Eddystone UID messages within the namespace {@code namespaceUid}.
     * @param namespaceUid The namespace Uid in hex format: e.g. "65AC11A8F8C51FF6476F"
     */
    public synchronized void scanEddystoneUid(String namespaceUid) {
        EddystoneUidMessageGenerator messageGenerator = new EddystoneUidMessageGenerator(namespaceUid);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Configures the scanner to scan all Eddystone UID messages within the namespaces {@code namespaceUids}.
     * @param namespaceUids A list of namespace Uids in hex format: e.g. "65AC11A8F8C51FF6476F"
     */
    public synchronized void scanEddystoneUid(List<String> namespaceUids) {
        List<BeaconMessageGenerator> newMessageGenerators = new ArrayList<>();
        // Add all message generators to the list if it does not exist yet in the list of all message generators.
        for (int i = 0; i < namespaceUids.size(); i++) {
            String uid = namespaceUids.get(i);
            EddystoneUidMessageGenerator messageGenerator = new EddystoneUidMessageGenerator(uid);
            if (!messageGenerators.contains(messageGenerator)) {
                newMessageGenerators.add(messageGenerator);
            }
        }
        addMessageGeneratorsAndRestartScannerIfNecessary(newMessageGenerators);
    }

    /**
     * Configures the scanner to scan all Eddystone UID messages within the namespace {@code namespaceUid}
     * that have instanceId {@code instanceId}.
     * @param namespaceUid The namespace Uid in hex format: e.g. "65AC11A8F8C51FF6476F"
     * @param instanceId The Eddystone UID instance id: e.g.: "1F2A"
     */
    public synchronized void scanEddystoneUid(String namespaceUid, String instanceId) {
        EddystoneUidMessageGenerator messageGenerator = new EddystoneUidMessageGenerator(namespaceUid, instanceId);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Configures the scanner to scan all Eddystone UID messages within the namespace {@code namespaceUid}
     * that have one of the instanceIds {@code instanceIds}.
     * @param namespaceUid The namespace Uid in hex format: e.g. "65AC11A8F8C51FF6476F"
     * @param instanceIds A list of Eddystone UID instances.
     */
    public synchronized void scanEddystoneUid(String namespaceUid, List<String> instanceIds) {
        List<BeaconMessageGenerator> newMessageGenerators = new ArrayList<>();
        // Add all message generators to the list if it does not exist yet in the list of all message generators.
        for (int i = 0; i < instanceIds.size(); i++) {
            String instance = instanceIds.get(i);
            EddystoneUidMessageGenerator messageGenerator = new EddystoneUidMessageGenerator(namespaceUid, instance);
            if (!messageGenerators.contains(messageGenerator)) {
                newMessageGenerators.add(messageGenerator);
            }
        }
        addMessageGeneratorsAndRestartScannerIfNecessary(newMessageGenerators);
    }

    /**
     * Configures the scanner to scan all Eddystone URL messages.
     */
    public synchronized void scanEddystoneUrl() {
        EddystoneUrlMessageGenerator messageGenerator = new EddystoneUrlMessageGenerator();
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Configures the scanner to scan all Eddystone URL messages with the URL {@code url}.
     * @param url The URL: E.g. https://goo.gl/Aq18zF
     */
    public synchronized void scanEddystoneUrl(String url) {
        EddystoneUrlMessageGenerator messageGenerator = new EddystoneUrlMessageGenerator(url);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Configures the scanner to scan all Eddystone URL messages that have one of the URLs {@code urls}.
     * @param urls A list of URLs: e.g. https://goo.gl/Aq18zF
     */
    public synchronized void scanEddystoneUrl(List<String> urls) {
        List<BeaconMessageGenerator> newMessageGenerators = new ArrayList<>();
        // Add all message generators to the list if it does not exist yet in the list of all message generators.
        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            EddystoneUidMessageGenerator messageGenerator = new EddystoneUidMessageGenerator(url);
            if (!messageGenerators.contains(messageGenerator)) {
                newMessageGenerators.add(messageGenerator);
            }
        }
        addMessageGeneratorsAndRestartScannerIfNecessary(newMessageGenerators);
    }

    /**
     * Adds the "JoinMe" advertising messages that FruityMesh beacons continuously send
     * to the list of scanned advertising messages.
     */
    public synchronized void scanJoinMeMessage() {
        BeaconJoinMeMessageGenerator messageGenerator = new BeaconJoinMeMessageGenerator();
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Adds the Asset tracking advertising messages to the list of scanned advertising messages.
     */
    public synchronized void scanAssetTrackingMessageV1() {
        AssetTrackingMessageV1Generator messageGenerator = new AssetTrackingMessageV1Generator();
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Adds Asset tracking messages with the asset ID {@code assetId} to the
     * ist of scanned advertising messages.
     * @param assetId The asset id to be scanned for
     */
    public synchronized void scanAssetTrackingMessageV1(int assetId) {
        AssetTrackingMessageV1Generator messageGenerator = new AssetTrackingMessageV1Generator(assetId);
        if (!messageGenerators.contains(messageGenerator)) {
            addMessageGeneratorAndRestartScannerIfNecessary(messageGenerator);
        }
    }

    /**
     * Returns the list of all message generators.
     * @return The list of all message generators.
     */
    List<BeaconMessageGenerator> getMessageGenerators() {
        return this.messageGenerators;
    }

    /**
     * Returns the scan cycle duration (in ms).
     * @return The scan cycle duration (in ms)
     */
    public long getScanPeriodInMillis() {
        return scanPeriodInMillis;
    }

    /**
     * Sets the scan cycle duration (in ms).
     * @param scanPeriodInMillis The scan cycle duration in ms.
     *                           Default value: 10l
     */
    public synchronized void setScanPeriodInMillis(long scanPeriodInMillis) {
        boolean restart = scanner.isRunning();
        stopScannerIfIsRunning(restart);
        this.scanPeriodInMillis = scanPeriodInMillis;
        startScannerIfDidRunBefore(restart);
    }

    /**
     * Returns the idle time between two consecutive scan cycles.
     * @return The idle time (in ms)
     */
    public long getBetweenScanPeriodInMillis() {
        return betweenScanPeriodInMillis;
    }

    /**
     * Sets the idle time (in ms) between two consecutive scan cycles.
     * @param betweenScanPeriodInMillis The idle time (in ms)
     *                                  Default value: 1
     */
    public synchronized void setBetweenScanPeriodInMillis(long betweenScanPeriodInMillis) {
        boolean restart = scanner.isRunning();
        stopScannerIfIsRunning(restart);
        this.betweenScanPeriodInMillis = betweenScanPeriodInMillis;
        startScannerIfDidRunBefore(restart);
    }

    private void stopScannerIfIsRunning(boolean restart) {
        if (restart) {
            scanner.stopScanning();
        }
    }

    private void startScannerIfDidRunBefore(boolean restart) {
        if (restart) {
            scanner.startScanning();
        }
    }
}
