//
//  RelutionHeatmapReportBuilder.java
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

package com.mway.bluerange.android.sdk.services.analytics;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconJoinMeMessage;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;
import com.mway.bluerange.android.sdk.core.reporting.BeaconMessageReportBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RelutionHeatmapReportBuilder implements BeaconMessageReportBuilder {

    // JSON objects
    private JSONArray intervals = null;
    private JSONObject interval = null;
    private JSONArray nodesInInterval = null;

    private String organizationUuid;

    // Interval data
    private static final long DEFAULT_INTERVAL_DURATION_IN_MS = 20000;
    private long intervalDurationInMs = DEFAULT_INTERVAL_DURATION_IN_MS;
    private Map<JSONObject, List<BeaconJoinMeMessage>> messagesInNode = new HashMap<>();

    public RelutionHeatmapReportBuilder(String organizationUuid) {
        this.organizationUuid = organizationUuid;
    }

    @Override
    public void newReport() throws BuildException {
        createNewReport();
    }

    private void createNewReport() {
        this.intervals = new JSONArray();
        this.interval = null;
        this.nodesInInterval = null;
        this.messagesInNode = new HashMap<>();
    }

    @Override
    public void addBeaconMessage(BeaconMessage message) throws BuildException {
        if (message instanceof BeaconJoinMeMessage) {
            BeaconJoinMeMessage joinMeMessage = (BeaconJoinMeMessage)message;
            try {
                addJoinMeMessage(joinMeMessage);
            } catch (JSONException e) {
                throw new BuildException();
            }
        }
    }

    private void addJoinMeMessage(BeaconJoinMeMessage joinMeMessage) throws JSONException {
        if (this.interval == null) {
            // Create interval when no interval exists yet.
            createNewInterval(joinMeMessage);
        } else if (intervalEndReached(joinMeMessage)) {
            // If an interval exists and the end of the interval has been reached,
            // finish the current interval and create a new one.
            addIntervalToReport();
            createNewInterval(joinMeMessage);
        }
        addJoinMeMessageToInterval(joinMeMessage);
    }

    private void createNewInterval(BeaconJoinMeMessage message) throws JSONException {
        this.interval = new JSONObject();
        this.nodesInInterval = new JSONArray();
        long messageTimeStamp = message.getTimestamp().getTime();
        this.interval.put("startTime", messageTimeStamp);
        this.interval.put("endTime", messageTimeStamp);
        this.interval.put("discoveredNodes", this.nodesInInterval);
    }

    private void addJoinMeMessageToInterval(BeaconJoinMeMessage message) throws JSONException {
        updateEndTime(message);
        updateDiscoveredNodes(message);
    }

    private void updateEndTime(BeaconJoinMeMessage message) throws JSONException {
        long messageTimestamp = message.getTimestamp().getTime();
        this.interval.put("endTime", messageTimestamp);
    }

    private void updateDiscoveredNodes(BeaconJoinMeMessage message) throws JSONException {
        int nodeId = message.getNodeId();
        JSONObject node = getNode(nodeId);
        if (node == null) {
            createAndAddNodeToDiscoveredNodes(nodeId);
            node = getNode(nodeId);
        }
        updateNode(node, message);
    }

    private JSONObject getNode(int nodeId) throws JSONException {
        for (int i = 0; i < this.nodesInInterval.length(); i++) {
            JSONObject node = this.nodesInInterval.getJSONObject(i);
            int foundNodeId = node.getInt("nodeId");
            if (foundNodeId == nodeId) {
                return node;
            }
        }
        return null;
    }

    private void createAndAddNodeToDiscoveredNodes(int nodeId) throws JSONException {
        JSONObject node = new JSONObject();
        node.put("nodeId", nodeId);
        node.put("packetCount", 0);
        node.put("avgRssi", 0);
        this.nodesInInterval.put(node);
    }

    private void updateNode(JSONObject node, BeaconJoinMeMessage message) throws JSONException {
        collectMessage(message, node);
        updateNodeEntries(node);
    }

    private void collectMessage(BeaconJoinMeMessage message, JSONObject node) {
        if (!messagesInNode.containsKey(node)) {
            messagesInNode.put(node, new ArrayList<BeaconJoinMeMessage>());
        }
        List<BeaconJoinMeMessage> messages = messagesInNode.get(node);
        messages.add(message);
    }

    private void updateNodeEntries(JSONObject node) throws JSONException {
        List<BeaconJoinMeMessage> messages = messagesInNode.get(node);
        int newPacketCount = messages.size();
        int newAvgRssi = 0;
        for (int i = 0;i < messages.size();i++) {
            BeaconJoinMeMessage message = messages.get(i);
            float rssi = message.getRssi();
            newAvgRssi += rssi;
        }
        newAvgRssi /= messages.size();
        node.put("packetCount", newPacketCount);
        node.put("avgRssi", newAvgRssi);
    }

    private boolean intervalEndReached(BeaconJoinMeMessage joinMeMessage) throws JSONException {
        long startTimeInMs = this.interval.getLong("startTime");
        long currentTimeInMs = joinMeMessage.getTimestamp().getTime();
        return (currentTimeInMs-startTimeInMs) >= DEFAULT_INTERVAL_DURATION_IN_MS;
    }

    private void addIntervalToReport() {
        this.intervals.put(this.interval);
        this.interval = null;
    }

    @Override
    public RelutionHeatmapReport buildReport() throws BuildException {
        if (this.interval != null) {
            addIntervalToReport();
        }
        RelutionHeatmapReport report = null;
        try {
            report = createActivityReport();
        } catch (JSONException e) {
            throw new BuildException();
        }
        return report;
    }

    private RelutionHeatmapReport createActivityReport() throws JSONException {
        JSONObject reportJSONObject = new JSONObject();
        reportJSONObject.put("report", this.intervals);
        reportJSONObject.put("organizationUUID", this.organizationUuid);
        RelutionHeatmapReport report = new RelutionHeatmapReport(reportJSONObject);
        return report;
    }

    public long getIntervalDurationInMs() {
        return intervalDurationInMs;
    }

    public void setIntervalDurationInMs(long intervalDurationInMs) {
        this.intervalDurationInMs = intervalDurationInMs;
    }
}
