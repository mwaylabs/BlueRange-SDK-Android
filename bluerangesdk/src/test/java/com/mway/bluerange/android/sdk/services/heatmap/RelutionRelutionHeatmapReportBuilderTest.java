//
//  RelutionRelutionHeatmapReportBuilderTest.java
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
