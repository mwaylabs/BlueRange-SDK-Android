//
//  RelutionRelutionHeatmapReportBuilderTest.java
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

package com.mway.bluerange.android.sdk.services.heatmap;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconJoinMeMessage;
import com.mway.bluerange.android.sdk.services.analytics.RelutionHeatmapReport;
import com.mway.bluerange.android.sdk.services.analytics.RelutionHeatmapReportBuilder;
import com.mway.bluerange.android.sdk.core.reporting.BeaconMessageReportBuilder;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 *
 */
public class RelutionRelutionHeatmapReportBuilderTest {

    private RelutionHeatmapReportBuilder builder;

    @Before
    public void setUp() {
        this.builder = new RelutionHeatmapReportBuilder("cc6fe26f-64ad-443a-b3d5-8474fe1c8577");
        // Initial builder configuration
        this.builder.setIntervalDurationInMs(30000);
    }

    @Test
    public void testBuildResultWithOneJoinMeMessage()
            throws BeaconMessageReportBuilder.BuildException, JSONException {
        // Start new report
        this.builder.newReport();
        // Add messages
        addArbitraryJoinMeMessage(new Date(0), -50);
        // Build report
        RelutionHeatmapReport actualReport = this.builder.buildReport();
        // Verify report
        verifyReport(actualReport,
                "{organizationUUID:cc6fe26f-64ad-443a-b3d5-8474fe1c8577," +
                "report:[" +
                        "{discoveredNodes:" +
                        "[" +
                        "{avgRssi:-50," +
                        "packetCount:1," +
                        "nodeId:0}" +
                        "]," +
                        "startTime:0," +
                        "endTime:0}" +
                        "]" +
                        "}");
    }

    @Test
    public void testBuildResultWithTwoJoinMeMessage()
            throws BeaconMessageReportBuilder.BuildException, JSONException {
        // Start new report
        this.builder.newReport();
        // Add messages
        addArbitraryJoinMeMessage(new Date(0), -50);
        addArbitraryJoinMeMessage(new Date(0), -50);
        // Build report
        RelutionHeatmapReport actualReport = this.builder.buildReport();
        // Verify report
        verifyReport(actualReport,
                "{organizationUUID:cc6fe26f-64ad-443a-b3d5-8474fe1c8577," +
        "report:[" +
                "{discoveredNodes:" +
                    "[" +
                        "{avgRssi:-50," +
                        "packetCount:2," +
                        "nodeId:0}" +
                    "]," +
                "startTime:0," +
                "endTime:0}" +
                "]" +
        "}");
    }

    @Test
    public void testReportWithMessagesInDifferentIntervals()
            throws BeaconMessageReportBuilder.BuildException, JSONException {

        // Start new report
        this.builder.newReport();
        // Add messages
        addArbitraryJoinMeMessage(new Date(0), -50);
        addArbitraryJoinMeMessage(new Date(30000), -50);
        // Build report
        RelutionHeatmapReport actualReport = this.builder.buildReport();
        // Verify report
        verifyReport(actualReport,
                "{organizationUUID:cc6fe26f-64ad-443a-b3d5-8474fe1c8577," +
        "report:[" +
                "{discoveredNodes:" +
                    "[" +
                        "{avgRssi:-50," +
                        "packetCount:1," +
                        "nodeId:0}" +
                    "]," +
                "startTime:0," +
                "endTime:0}," +
                "{discoveredNodes:" +
                    "[" +
                        "{avgRssi:-50," +
                        "packetCount:1," +
                        "nodeId:0}" +
                    "]," +
                "startTime:30000," +
                "endTime:30000}" +
                "]" +
        "}");
    }

    @Test
    public void testDifferentRssisInMessages() throws BeaconMessageReportBuilder.BuildException, JSONException {
        // Start first report
        this.builder.newReport();
        // Add messages
        addArbitraryJoinMeMessage(new Date(0), -50);
        addArbitraryJoinMeMessage(new Date(0), -100);
        // Build report
        RelutionHeatmapReport actualReport = this.builder.buildReport();
        // Verify report
        verifyReport(actualReport,
                "{organizationUUID:cc6fe26f-64ad-443a-b3d5-8474fe1c8577," +
                "report:[" +
                        "{discoveredNodes:" +
                        "[" +
                        "{avgRssi:-75," +
                        "packetCount:2," +
                        "nodeId:0}" +
                        "]," +
                        "startTime:0," +
                        "endTime:0}" +
                        "]" +
                        "}");
    }

    @Test
    public void testTwoReports() throws BeaconMessageReportBuilder.BuildException, JSONException {
        // Start first report
        this.builder.newReport();
        // Add messages
        addArbitraryJoinMeMessage(new Date(0), -50);
        addArbitraryJoinMeMessage(new Date(0), -50);
        // Build report
        RelutionHeatmapReport actualReport = this.builder.buildReport();
        // Verify report
        verifyReport(actualReport,
                "{organizationUUID:cc6fe26f-64ad-443a-b3d5-8474fe1c8577," +
                "report:[" +
                        "{discoveredNodes:" +
                            "[" +
                                "{avgRssi:-50," +
                                "packetCount:2," +
                                "nodeId:0}" +
                            "]," +
                        "startTime:0," +
                        "endTime:0}" +
                        "]" +
                "}");

        // Start second report
        this.builder.newReport();
        // Add messages
        addArbitraryJoinMeMessage(new Date(0), -50);
        // Build report
        RelutionHeatmapReport actualReport2 = this.builder.buildReport();
        // Verify report
        verifyReport(actualReport2,
                "{organizationUUID:cc6fe26f-64ad-443a-b3d5-8474fe1c8577," +
                "report:[" +
                        "{discoveredNodes:" +
                            "[" +
                                "{avgRssi:-50," +
                                "packetCount:1," +
                                "nodeId:0}" +
                            "]," +
                        "startTime:0," +
                        "endTime:0}" +
                        "]" +
                "}");
    }

    private void addArbitraryJoinMeMessage(Date date, int rssi) throws BeaconMessageReportBuilder.BuildException {
        BeaconJoinMeMessage message = createArbitraryJoinMeMessage(date, rssi);
        this.builder.addBeaconMessage(message);
    }

    private BeaconJoinMeMessage createArbitraryJoinMeMessage(Date date, int rssi) {
        int sender = 0;
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
        BeaconJoinMeMessage message = new BeaconJoinMeMessageFake(date, rssi, sender, clusterId, clusterSize,
                freeInConnections, freeOutConnections, batteryRuntime, txPower,
                deviceType, hopsToSink, meshWriteHandle, ackField);
        return message;
    }

    private void verifyReport(RelutionHeatmapReport actualReport, String expectedJsonString)
            throws JSONException {
        String actualJSONReport = actualReport.getJsonObject().toString();
        String expectedJSONObject = new JSONObject(expectedJsonString).toString();
        Assert.assertEquals(expectedJSONObject, actualJSONReport);
        System.out.println(actualJSONReport);
    }
}
