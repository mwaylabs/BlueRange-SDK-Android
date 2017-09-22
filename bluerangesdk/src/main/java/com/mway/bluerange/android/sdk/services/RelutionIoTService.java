//
//  RelutionIoTService.java
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

package com.mway.bluerange.android.sdk.services;

import com.mway.bluerange.android.sdk.core.advertising.BeaconAdvertiser;
import com.mway.bluerange.android.sdk.common.BluetoothDisabledException;
import com.mway.bluerange.android.sdk.core.advertising.PeripheralAdvertisingNotSupportedException;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScanner;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerConfig;
import com.mway.bluerange.android.sdk.core.scanning.BeaconMessageScannerSimulator;
import com.mway.bluerange.android.sdk.core.scanning.IBeaconMessageScanner;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.RelutionTagMessageV1;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNodeDefaultReceiver;
import com.mway.bluerange.android.sdk.core.triggering.BeaconAction;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionDebugListener;
import com.mway.bluerange.android.sdk.core.triggering.BeaconActionListener;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.content.RelutionContentAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.notification.RelutionNotificationAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag.RelutionTagAction;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.services.relution.Relution;
import com.mway.bluerange.android.sdk.services.analytics.RelutionHeatmapService;
import com.mway.bluerange.android.sdk.services.configuration.RelutionScanConfigLoader;
import com.mway.bluerange.android.sdk.services.configuration.RelutionScanConfigLoaderImpl;
import com.mway.bluerange.android.sdk.services.relution.RelutionImpl;
import com.mway.bluerange.android.sdk.services.relution.RelutionStub;
import com.mway.bluerange.android.sdk.services.tags.RelutionTagInfo;
import com.mway.bluerange.android.sdk.services.tags.RelutionTagInfoRegistry;
import com.mway.bluerange.android.sdk.services.tags.RelutionTagInfoRegistryImpl;
import com.mway.bluerange.android.sdk.services.campaigns.RelutionCampaignService;
import com.mway.bluerange.android.sdk.services.trigger.BeaconTrigger;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

import java.util.ArrayList;
import java.util.List;

/**
 * This service class unites all features that are currently supported by the SDK and provides an
 * easy to use interface, so that it is not necessary to know all the details of the underlying
 * message processing architecture. Before starting this service with one of the start methods
 * defined in the base class, you must call the {@link #setLoginData} method, where you pass the
 * Relution server URL, the organization UUID and the login data. After the service has been
 * started, you can register different observers to get informed about incoming beacon messages,
 * Relution tags or executed actions. Moreover, you can use this class to calibrate iBeacon
 * messages.
 */
public class RelutionIoTService extends BlueRangeService {

    // Logging
    private static final String LOG_TAG = "RelutionIoTService";
    private static Tracer tracer = Tracer.getInstance();

    // State
    private static boolean running = false;

    // Components
    private static Relution relution;
    private static IBeaconMessageScanner scanner;
    private static RelutionScanConfigLoader scanConfigLoader;
    private static RelutionCampaignService relutionCampaignService;
    private static RelutionTagInfoRegistry relutionTagInfoRegistry;
    private static BeaconTrigger policyTrigger;
    private static RelutionHeatmapService heatmapService;

    // Login observer
    private static List<LoginObserver> loginObservers = new ArrayList<>();

    // Message observers
    private static List<BeaconMessageObserver> messageObservers = new ArrayList<>();

    // Action observers
    private static List<BeaconTagActionObserver> tagActionObservers
            = new ArrayList<>();
    private static List<BeaconContentActionObserver> contentActionObservers
            = new ArrayList<>();
    private static List<BeaconNotificationActionObserver> notificationActionObservers
            = new ArrayList<>();

    // Debug action observers
    private static List<BeaconTagActionDebugObserver> tagActionDebugObservers
            = new ArrayList<>();
    private static List<BeaconContentActionDebugObserver> contentActionDebugObservers
            = new ArrayList<>();
    private static List<BeaconNotificationActionDebugObserver> notificationActionDebugObservers
            = new ArrayList<>();

    // Relution tag observers
    private static List<RelutionTagObserver> relutionTagObservers = new ArrayList<>();

    // Policy trigger observers
    private static List<PolicyTriggerObserver> policyTriggerObservers = new ArrayList<>();

    // Observer interfaces

    public interface LoginObserver {
        void onLoginSucceeded();
        void onLoginFailed();
        void onRelutionError();
    }

    private static void addLoginObserver(LoginObserver loginObserver) {
        loginObservers.add(loginObserver);
    }

    public interface BeaconMessageObserver {
        void onMessageReceived(BeaconMessage message);
    }

    /**
     * Adds a {@link BeaconMessageObserver} observer to the list of all observers.
     * The onMessageReceived method will be called, whenever a message was received that was
     * added to a beacon in the Relution platform.
     * Notice: If you want to scan for messages that were not added to a beacon
     * on the Relution platform, you must create your own {@link BeaconMessageScanner} instance,
     * configure it to scan for these messages and register a receiver
     * to get informed about these messages.
     * @param observer The observer that will be added to the list of all observers.
     */
    public synchronized static void addBeaconMessageObserver(BeaconMessageObserver observer) {
        messageObservers.add(observer);
    }

    public interface BeaconTagActionObserver {
        void onTagActionExecuted(RelutionTagAction tagAction);
    }
    /**
     * Adds a {@link BeaconTagActionObserver} observer to the list of all observers.
     * The onTagActionExecuted method will be called, whenever an 'visited' action was executed.
     * @param observer The observer that will be added to the list of all observers.
     */
    public synchronized static void addBeaconTagActionObserver(BeaconTagActionObserver observer) {
        tagActionObservers.add(observer);
    }

    public interface BeaconContentActionObserver {
        void onContentActionExecuted(RelutionContentAction contentAction);
    }
    /**
     * Adds a {@link BeaconContentActionObserver} observer to the list of all observers.
     * The onContentActionExecuted method will be called, whenever a 'content' action was executed.
     * @param observer The observer that will be added to the list of all observers.
     */
    public synchronized static void addBeaconContentActionObserver(
            BeaconContentActionObserver observer) {
        contentActionObservers.add(observer);
    }

    public interface BeaconNotificationActionObserver {
        void onNotificationActionExecuted(RelutionNotificationAction notificationAction);
    }
    /**
     * Adds a {@link BeaconNotificationActionObserver} observer to the list of all observers.
     * The onNotificationActionExecuted method will be called, whenever a 'notification'
     * action was executed.
     * @param observer The observer that will be added to the list of all observers.
     */
    public synchronized static void addBeaconNotificationActionObserver(
            BeaconNotificationActionObserver observer) {
        notificationActionObservers.add(observer);
    }

    // Debug observers
    public interface BeaconTagActionDebugObserver {
        void onTagActionReceived(RelutionTagAction tagAction);
    }
    public synchronized static void addBeaconTagActionDebugObserver(
            BeaconTagActionDebugObserver observer) {
        tagActionDebugObservers.add(observer);
    }
    public interface BeaconContentActionDebugObserver {
        void onContentActionReceived(RelutionContentAction contentAction);
    }
    public synchronized static void addBeaconContentActionDebugObserver(
            BeaconContentActionDebugObserver observer) {
        contentActionDebugObservers.add(observer);
    }
    public interface BeaconNotificationActionDebugObserver {
        void onNotificationActionReceived(RelutionNotificationAction notificationAction);
    }
    public synchronized static void addBeaconNotificationActionDebugObserver(
            BeaconNotificationActionDebugObserver observer) {
        notificationActionDebugObservers.add(observer);
    }

    // Relution Tag observer
    public interface RelutionTagObserver {
        void onTagReceived(long tag, RelutionTagMessageV1 message);
    }
    /**
     * Adds a {@link RelutionTagObserver} observer to the list of all observers.
     * The onTagReceived method will be called, whenever a Relution tag was received from a beacon.
     * @param observer The observer that will be added to the list of all observers.
     */
    public synchronized static void addRelutionTagObserver(RelutionTagObserver observer) {
        relutionTagObservers.add(observer);
    }

    // Policy trigger observer
    public interface PolicyTriggerObserver {
        void onBeaconActive();
        void onBeaconInactive();
        void onNewDistance(float distance);
    }

    public synchronized static void addPolicyTriggerObserver(PolicyTriggerObserver observer) {
        policyTriggerObservers.add(observer);
    }

    /**
     * If using the offline mode, the login will always succeed. However, all requests
     * to the server are mocked basically doing nothing. This mode can be important,
     * when the user does not have Internet access, however wants to use basic functionality
     * like scanning BeaconJoinMe or Asset messages.
     * @param offlineMode true, if offlineMode should be enabled.
     * @return The service instance.
     */
    public RelutionIoTService setOfflineMode(boolean offlineMode) {
        RelutionIoTServiceConfig.offlineMode = offlineMode;
        return this;
    }

    /**
     * Sets the required Relution URL, organization UUID, Username and Password.
     * Notice: This method must be called before the service is started.
     * @param baseUrl The URL to the Relution server, e.g. "https://iot.relution.io".
     * @param username The Relution username you use to login.
     * @param password The Relution password you use to login.
     * @return The same instance of this class.
     */
    public RelutionIoTService setLoginData(String baseUrl,
                                           String username, String password,
                                           LoginObserver observer) {
        RelutionIoTServiceConfig.baseUrl = baseUrl;
        RelutionIoTServiceConfig.username = username;
        RelutionIoTServiceConfig.password = password;
        if (observer != null) {
            loginObservers.clear();
            addLoginObserver(observer);
        }
        return this;
    }

    /**
     * Enables/disables logging debug messages. Disabling logging might increase the performance
     * of your app.
     * @param enabled true, if logging should be enabled.
     * @return The same instance of this class.
     */
    public RelutionIoTService setLoggingEnabled(boolean enabled) {
        RelutionIoTServiceConfig.loggingEnabled = enabled;
        return this;
    }

    /**
     * Enables/disables executing actions that are defined inside a campaign and
     * assigned to specific beacons in Relution.
     * @param enabled true, if action execution should be enabled.
     * @return The same instance of this class.
     */
    public RelutionIoTService setCampaignActionTriggerEnabled(boolean enabled) {
        RelutionIoTServiceConfig.campaignActionTriggerEnabled = enabled;
        return this;
    }

    /**
     * Enables/disables sending specific advertising messages being used to generate and
     * display the heatmap in Relution.
     * @param enabled true, if heatmap generation should be enabled.
     * @return The same instance of this class.
     */
    public RelutionIoTService setHeatmapGenerationEnabled(boolean enabled) {
        RelutionIoTServiceConfig.heatmapGenerationEnabled = enabled;
        return this;
    }

    /**
     * Enables/disables periodically sending status reports to Relution. The
     * reports collect the received signal strength of all detected beacons for each
     * small time interval since this service has been started.
     * @param enabled true, if heatmap reporting should be enabled.
     * @return The same instance of this class.
     */
    public RelutionIoTService setSendingAnalyticsDataEnabled(boolean enabled) {
        RelutionIoTServiceConfig.sendingAnalyticsDataEnabled = enabled;
        return this;
    }

    public RelutionIoTService setPolicyTriggerEnabled(boolean policyTriggerEnabled) {
        RelutionIoTServiceConfig.policyTriggerEnabled = policyTriggerEnabled;
        return this;
    }

    public RelutionIoTService setRelutionTagObservingEnabled(boolean relutionTagObservingEnabled) {
        RelutionIoTServiceConfig.relutionTagObservingEnabled = relutionTagObservingEnabled;
        return this;
    }

    @Override
    protected void onStarted() {
        try {
            if (isRunning()) {
                stopWithoutRemovingObservers();
            }
            initTracing();
            login();
            publishLoginSucceeded();
            //scanner = createScannerSimulator();
            scanner = createScanner();
            startScanConfigLoader();
            startReporter(scanner);
            startTrigger(scanner);
            startPolicyTrigger(scanner);
            startScanner(scanner);
            startAdvertiser();
            initRelutionTagRegistry(scanner);
            running = true;
        } catch (RelutionImpl.RelutionException e) {
            publishLoginRelutionError();
        } catch (RelutionImpl.LoginException e) {
            publishLoginFailed();
        }
    }

    public static boolean isRunning() {
        return running;
    }

    private void login() throws RelutionImpl.RelutionException, RelutionImpl.LoginException {
        if (RelutionIoTServiceConfig.offlineMode) {
            relution = new RelutionStub();
        } else {
            relution = new RelutionImpl(getContext(),
                    RelutionIoTServiceConfig.baseUrl,
                    RelutionIoTServiceConfig.username,
                    RelutionIoTServiceConfig.password);
        }
        RelutionIoTServiceConfig.organizationUuid = relution.getOrganizationUuid();
    }

    private void initTracing() {
        if (RelutionIoTServiceConfig.loggingEnabled) {
            Tracer.setEnabled(true);
        } else {
            Tracer.setEnabled(false);
        }
    }

    private IBeaconMessageScanner createScannerSimulator() {
        BeaconMessageScannerSimulator scanner = new BeaconMessageScannerSimulator();

        // iBeacons
        scanner.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -75);
        scanner.simulateIBeaconWithRssi("92407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -82);
        scanner.simulateIBeaconWithRssi("29407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -10);
        scanner.simulateIBeaconWithRssi("b9407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -10);
        scanner.simulateIBeaconWithRssi("95407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -10);
        // Delayed notification
        scanner.simulateIBeaconWithRssi("19407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -50);
        scanner.simulateIBeaconWithRssi("94407f30-f5f8-466e-aff9-25556b57fe6d", 45, 1, -10);

        // Eddystone
        scanner.simulateEddystoneUidWithRssi("65AC11A8F8C51FF6476F", "1234", -25);
        scanner.simulateEddystoneUrlWithRssi("http://google.de/", -34);

        // Stress test
        /*for (int i = 0; i < 100; i++) {
            scanner.simulateIBeaconWithRssi(UUID.randomUUID().toString(), 45, 1, -75);
        }*/

        // Join me messages
        scanner.simulateJoinMe(9999, -45);
        scanner.simulateJoinMe(1010, -75);

        // Relution tag messages
        scanner.simulateRelutionTagsV1(new long[]{1, 2});

        // Relution Tag V2 messages
        scanner.simulateRelutionTags("65AC11A8F8C51FF6476F", new int[] {3, 4});
        scanner.simulateRelutionTagsWithRssi("75AC11A8F8C51FF6476F", new int[] {10, 20}, -44);

        scanner.setRepeat(true);
        //scanner.setRepeatInterval(20000l);
        scanner.setRepeatInterval(500l);
        scanner.addRssiNoise();
        scanner.addReceiver(new BeaconMessageStreamNodeDefaultReceiver() {
            @Override
            public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage message) {
                publishBeacons(message);
            }
        });
        return scanner;
    }

    private IBeaconMessageScanner createScanner() {
        final BeaconMessageScanner scanner = new BeaconMessageScanner(this.getContext());
        scanner.addReceiver(new BeaconMessageStreamNodeDefaultReceiver() {
            @Override
            public void onReceivedMessage(BeaconMessageStreamNode senderNode, BeaconMessage
                    message) {
                // Publish all messages
                publishBeacons(message);

                // Publish Relution Tag messages
                if (message instanceof RelutionTagMessageV1) {
                    RelutionTagMessageV1 relutionTagMessage = (RelutionTagMessageV1) message;
                    List<Long> tags = relutionTagMessage.getTags();
                    for (long tag : tags) {
                        publishRelutionTag(tag, relutionTagMessage);
                    }
                }
            }
        });

        BeaconMessageScannerConfig config = scanner.getConfig();
        config.scanJoinMeMessage();
        config.setScanPeriodInMillis(RelutionIoTServiceConfig.scanPeriodInMillis);
        config.setBetweenScanPeriodInMillis(RelutionIoTServiceConfig.betweenScanPeriodInMillis);
        scanner.setMeshInactiveTimeoutInMs(RelutionIoTServiceConfig.scannerMeshInactiveTimeoutInMs);
        scanner.setTimeBetweenBluetoothDisableAndEnableInMs(RelutionIoTServiceConfig
                .scannerBluetoothRestartDurationInMs);
        scanner.setPollingTimeToCheckStateChangeInMs(RelutionIoTServiceConfig
                .scannerBluetoothRestartPollingDurationInMs);

        // By default, we scan all Eddystone URL messages.
        config.scanEddystoneUrl();

        // By default, scan tracked asset messages.
        config.scanAssetTrackingMessageV1();

        //config.scanIBeacon("b9407f30-f5f8-466e-aff9-25556b57fe6d");

        //config.scanEddystoneUid("65AC11A8F8C51FF6476F", "13037496146791");
        //config.scanEddystoneUid("00010203040506070809", "1F2A");
        //config.scanEddystoneUid();
        //config.scanEddystoneUid("00010203040506070809");
        //config.scanEddystoneUid();
        //config.scanEddystoneUrl("http://goo.gl/HaUfdz");
        //config.scanEddystoneUrl();

        //config.scanRelutionTagsV1(new long[] {1,2});
        //config.scanRelutionTagsV1();

        //config.scanRelutionTags("00010203040506070809");
        /*try {
            config.scanRelutionTags("00010203040506070809", new int[] {66});
        } catch (Exception e) {
            tracer.logDebug("", "Adding relution tags to scanner failed. " + e.getMessage());
        }*/

        return scanner;
    }

    private void startScanConfigLoader() {
        scanConfigLoader = new RelutionScanConfigLoaderImpl(
                relution,
                scanner,
                RelutionIoTServiceConfig.triggerWaitTimeBetweenUuidRegistrySynchronizationInMs);
        scanConfigLoader.start();
    }

    private void startReporter(IBeaconMessageScanner scanner) {
        if (RelutionIoTServiceConfig.sendingAnalyticsDataEnabled) {
            heatmapService =
                    new RelutionHeatmapService(
                            getContext(),
                            scanner,
                            relution,
                            RelutionIoTServiceConfig.reporterIntervalDurationInStatusReportsInMs,
                            RelutionIoTServiceConfig.reporterTimeBetweenStatusReportsInMs,
                            RelutionIoTServiceConfig.reporterPollingTimeToWaitForReceiverInMs);
            heatmapService.start();
        }
    }

    private void startTrigger(IBeaconMessageScanner scanner) {
        if (RelutionIoTServiceConfig.campaignActionTriggerEnabled) {
            relutionCampaignService = new RelutionCampaignService(
                    getContext(),
                    scanner,
                    relution,
                    RelutionIoTServiceConfig.triggerMaximumQueueSize,
                    RelutionIoTServiceConfig.triggerPollingTimeForCheckingRegistryAvailable,
                    RelutionIoTServiceConfig.triggerPollingTimeForCheckingDelayedActionsInMs,
                    RelutionIoTServiceConfig.triggerPollingTimeForCheckingLocksInMs,
                    RelutionIoTServiceConfig.triggerAggregateDurationInMs);
            relutionCampaignService.addDebugActionListener(new BeaconActionDebugListener() {
                @Override
                public void onActionExecutionStarted(BeaconAction action) {
                    if (action instanceof RelutionTagAction) {
                        RelutionTagAction tagAction = (RelutionTagAction) action;
                        tracer.logDebug(LOG_TAG, "Visited tag = " + tagAction.getTag());
                        publishTagAction(tagAction);
                    } else if (action instanceof RelutionContentAction) {
                        RelutionContentAction contentAction = (RelutionContentAction) action;
                        tracer.logDebug(LOG_TAG, "Received content = " + contentAction.getContent
                                ());
                        publishContentAction(contentAction);
                    } else if (action instanceof RelutionNotificationAction) {
                        RelutionNotificationAction notificationAction =
                                (RelutionNotificationAction) action;
                        tracer.logDebug(LOG_TAG, "Notification received: " + notificationAction
                                .getContent());
                        publishNotificationAction(notificationAction);
                    }
                }
            });
            relutionCampaignService.addActionListener(new BeaconActionListener() {
                @Override
                public void onActionTriggered(BeaconAction action) {
                    if (action instanceof RelutionContentAction) {
                        RelutionContentAction contentAction = (RelutionContentAction) action;
                        tracer.logDebug(LOG_TAG, "Received content = " + contentAction.getContent
                                ());
                        publishContentActionExecuted(contentAction);
                    } else if (action instanceof RelutionNotificationAction) {
                        RelutionNotificationAction notificationAction =
                                (RelutionNotificationAction) action;
                        tracer.logDebug(LOG_TAG, "Notification received: " + notificationAction
                                .getContent());
                    } else if (action instanceof RelutionTagAction) {
                        RelutionTagAction tagAction = (RelutionTagAction) action;
                        tracer.logDebug(LOG_TAG, "Visited tag = " + tagAction.getTag());
                        publishTagActionExecuted(tagAction);
                    }

                }
            });
            relutionCampaignService.start();
        }
    }

    private void startPolicyTrigger(IBeaconMessageScanner scanner) {
        if (RelutionIoTServiceConfig.policyTriggerEnabled) {
            policyTrigger = new BeaconTrigger(tracer, scanner, getContext());
            policyTrigger.addRelutionTagTrigger(1L);
            policyTrigger.addRelutionTagTrigger(2L);
            policyTrigger.addRelutionTagTrigger(3L);
            policyTrigger.addRelutionTagTrigger(4L);
            policyTrigger.addRelutionTagTrigger(5L);
            policyTrigger.addObserver(new BeaconTrigger.BeaconTriggerObserver() {
                @Override
                public void onBeaconActive(BeaconMessage message) {
                    publishBeaconActive();
                }

                @Override
                public void onBeaconInactive(BeaconMessage message) {
                    publishBeaconInactive();
                }

                @Override
                public void onNewDistance(BeaconMessage message, float distance) {
                    publishDistance(distance);
                }
            });
        }
    }

    private void startScanner(IBeaconMessageScanner scanner) {
        scanner.startScanning();
    }

    private void initRelutionTagRegistry(IBeaconMessageScanner scanner) {
        if (RelutionIoTServiceConfig.relutionTagObservingEnabled) {
            RelutionTagInfoRegistryImpl registry = new RelutionTagInfoRegistryImpl(relution);
            registry.setWaitTimeBetweenRelutionTagRegistrySynchronizationInMs(
                    RelutionIoTServiceConfig.relutionTagRegistrySynchronizationTimeInMs);
            relutionTagInfoRegistry = registry;
            //relutionTagInfoRegistry = new RelutionTagInfoRegistryStub();
            relutionTagInfoRegistry.continuouslyUpdateRegistry();
        }
    }

    private void startAdvertiser() {
        if (RelutionIoTServiceConfig.heatmapGenerationEnabled) {
            try {
                BeaconAdvertiser advertiser = new BeaconAdvertiser(getContext().getApplicationContext());
                advertiser.startAdvertisingDiscoveryMessage();
            } catch (PeripheralAdvertisingNotSupportedException e) {
                tracer.logDebug(LOG_TAG, "This device does not support advertising in peripheral mode!");
            } catch (BluetoothDisabledException e) {
                tracer.logDebug(LOG_TAG, "Bluetooth is disabled! App will not advertise messages!");
            } catch(Throwable throwable) {
                tracer.logDebug(LOG_TAG, "An unknown problem occurred when starting the advertiser.");
            }
        }
    }

    private synchronized static void publishLoginSucceeded() {
        for (LoginObserver observer : loginObservers) {
            observer.onLoginSucceeded();
        }
    }

    private synchronized static void publishLoginFailed() {
        for (LoginObserver observer : loginObservers) {
            observer.onLoginFailed();
        }
    }

    private synchronized static void publishLoginRelutionError() {
        for (LoginObserver observer : loginObservers) {
            observer.onRelutionError();
        }
    }

    private synchronized static void publishBeacons(
            BeaconMessage beaconMessage) {
        for (BeaconMessageObserver messageObserver : messageObservers) {
            messageObserver.onMessageReceived(beaconMessage);
        }
    }

    private synchronized static void publishTagAction(
            RelutionTagAction tagAction) {
        for (BeaconTagActionDebugObserver observer : tagActionDebugObservers) {
            observer.onTagActionReceived(tagAction);
        }
    }

    private synchronized static void publishTagActionExecuted(
            RelutionTagAction tagAction) {
        for (BeaconTagActionObserver observer : tagActionObservers) {
            observer.onTagActionExecuted(tagAction);
        }
    }

    private synchronized static void publishContentAction(
            RelutionContentAction contentAction) {
        for (BeaconContentActionDebugObserver observer : contentActionDebugObservers) {
            observer.onContentActionReceived(contentAction);
        }
    }

    private synchronized static void publishContentActionExecuted(
            RelutionContentAction contentAction) {
        for (BeaconContentActionObserver observer : contentActionObservers) {
            observer.onContentActionExecuted(contentAction);
        }
    }

    private synchronized static void publishNotificationAction(
            RelutionNotificationAction notificationAction) {
        for (BeaconNotificationActionDebugObserver observer : notificationActionDebugObservers) {
            observer.onNotificationActionReceived(notificationAction);
        }
    }

    private synchronized static void publishNotificationActionExecuted(
            RelutionNotificationAction notificationAction) {
        for (BeaconNotificationActionObserver observer : notificationActionObservers) {
            observer.onNotificationActionExecuted(notificationAction);
        }
    }

    private synchronized static void publishRelutionTag(long tag, RelutionTagMessageV1 message) {
        for (RelutionTagObserver observer : relutionTagObservers) {
            observer.onTagReceived(tag, message);
        }
    }

    private synchronized static void publishBeaconActive() {
        for (PolicyTriggerObserver observer : policyTriggerObservers) {
            observer.onBeaconActive();
        }
    }

    private synchronized static void publishBeaconInactive() {
        for (PolicyTriggerObserver observer : policyTriggerObservers) {
            observer.onBeaconInactive();
        }
    }

    private synchronized static void publishDistance(float distance) {
        for (PolicyTriggerObserver observer : policyTriggerObservers) {
            observer.onNewDistance(distance);
        }
    }

    /**
     * Requests the Relution tag information for the passed tag from Relution.
     * @param tag The tag identifier, which the Relution tag information should be requested for.
     * @return A new RelutionTagInfo instance containing the information for the tag.
     * @throws RelutionTagInfoRegistry.RelutionTagInfoRegistryNoInfoFound will be thrown, when
     * this tag has not been defined in Relution.
     */
    public static RelutionTagInfo getTagInfoForTag(long tag)
            throws RelutionTagInfoRegistry.RelutionTagInfoRegistryNoInfoFound {
        if (!RelutionIoTServiceConfig.relutionTagObservingEnabled) {
            throw new RelutionTagInfoRegistry.RelutionTagInfoRegistryNoInfoFound();
        }
        RelutionTagInfo relutionTagInfo = relutionTagInfoRegistry.getRelutionTagInfoForTag(tag);
        return relutionTagInfo;
    }

    /**
     * Changes the txPower field of an iBeacon message assigned to a beacon in Relution. This
     * method can be used to calibrate an iBeacon message for the assigned beacon in order
     * to improve distance estimation used e.g. in campaign actions that make use of a distance threshold.
     * @param iBeacon The iBeacon which should be calibrated.
     * @param txPower The txPower field, which is the same as the received signal strength (RSSI)
     *                at one meter distance away from the beacon.
     */
    public static void calibrateIBeacon(IBeacon iBeacon, float txPower) {
        tracer.logDebug(LOG_TAG, "Calibrate iBeacon with UUID "
                + iBeacon.getUuid().toString() + " to RSSI: " + txPower);
        try {
            relution.setCalibratedRssiForIBeacon(iBeacon, (int)txPower);
        } catch (Exception e) {
            tracer.logDebug(LOG_TAG, "Calibrating RSSI failed.");
        }
    }

    /**
     * Returns the organization UUID
     * @return the organization UUID.
     */
    public static String getOrganizationUuid() {
        return RelutionIoTServiceConfig.organizationUuid;
    }

    public static String getUsername() {
        return RelutionIoTServiceConfig.username;
    }

    public static String getHostname() {
        return RelutionIoTServiceConfig.baseUrl;
    }

    @Override
    public void stop() {
        stopWithoutRemovingObservers();
        removeObservers();
    }

    private void stopWithoutRemovingObservers() {
        // If stop is called we stop all running threads.
        if (isRunning()) {
            stopMessageProcessingComponents();
            running = false;
        }
    }

    private void stopMessageProcessingComponents() {
        stopIBeaconCalibrator();
        stopRelutionTagRegistry();
        stopAdvertiser();
        stopScanConfigLoader();
        stopScanner();
        stopPolicyTrigger();
        stopTrigger();
        stopReporter();
    }

    private void removeObservers() {
        loginObservers.clear();

        messageObservers.clear();

        tagActionObservers.clear();
        contentActionObservers.clear();
        notificationActionObservers.clear();

        tagActionDebugObservers.clear();
        contentActionDebugObservers.clear();
        notificationActionDebugObservers.clear();

        relutionTagObservers.clear();
        policyTriggerObservers.clear();
    }

    private void stopIBeaconCalibrator() {
        // Nothing has to be stopped.
    }

    private void stopRelutionTagRegistry() {
        relutionTagInfoRegistry.stopUpdatingRegistry();
    }

    private void stopAdvertiser() {
        // Nothing has to be stopped.
    }

    private void stopScanConfigLoader() {
        scanConfigLoader.stop();
    }

    private void stopScanner() {
        scanner.stopScanning();
    }

    private void stopPolicyTrigger() {
        if (policyTrigger != null) {
            policyTrigger.stop();
        }
    }

    private void stopTrigger() {
        relutionCampaignService.stop();
    }

    private void stopReporter() {
        heatmapService.stop();
    }

    // Getters and setters

    public static IBeaconMessageScanner getScanner() {
        return scanner;
    }
}
