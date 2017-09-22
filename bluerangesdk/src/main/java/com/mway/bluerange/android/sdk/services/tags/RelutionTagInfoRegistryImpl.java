//
//  RelutionTagInfoRegistryImpl.java
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

package com.mway.bluerange.android.sdk.services.tags;

import com.mway.bluerange.android.sdk.services.relution.Relution;
import com.mway.bluerange.android.sdk.services.relution.model.RelutionTagInfos;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the default implementation for the {@link RelutionTagInfoRegistry} interface.
 */
public class RelutionTagInfoRegistryImpl implements RelutionTagInfoRegistry {

    // Logging
    private static final String LOG_TAG = "RelutionTagInfoRegistryImpl";
    private ITracer tracer = Tracer.getInstance();

    // Thread
    private Thread thread;

    // UUID Synchronization
    public static final long DEFAULT_WAIT_TIME_BETWEEN_RELUTION_TAG_REGISTRY_SYNCHRONIZATIONS_IN_MS
            = 20000L; // 20 seconds
    private long waitTimeBetweenRelutionTagRegistrySynchronizationInMs
            = DEFAULT_WAIT_TIME_BETWEEN_RELUTION_TAG_REGISTRY_SYNCHRONIZATIONS_IN_MS;

    private Relution relution;
    private Map<Long, RelutionTagInfo> tagToInfo = new HashMap<>();

    public RelutionTagInfoRegistryImpl(Relution relution) {
        this.relution = relution;
    }

    @Override
    public void continuouslyUpdateRegistry() {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        tryLoadingRegistry();
                        waitUntilNextSynchronization();
                    }
                } catch (InterruptedException e) {
                    // An interrupt should stop the thread.
                }
            }
        });
        this.thread.setName("RelutionTagInfoRegistry");
        this.thread.start();
    }

    private void tryLoadingRegistry() {
        try {
            if (!relution.isServerAvailable()) {
                tracer.logWarning(LOG_TAG, "Server to calibrate beacon cannot be accessed!");
                return;
            }
            RelutionTagInfos tagInfos = relution.getRelutionTagInfos();
            tagToInfo = parseJsonObject(tagInfos.getJsonArray());
            tracer.logDebug(LOG_TAG, "Received Relution Tag list: " + tagToInfo.keySet());
        } catch (Exception e) {
            tracer.logWarning(LOG_TAG, "Relution tag registry not available!");
        }
    }

    private void waitUntilNextSynchronization() throws InterruptedException {
        Thread.sleep(waitTimeBetweenRelutionTagRegistrySynchronizationInMs);
    }

    private Map<Long, RelutionTagInfo> parseJsonObject(JSONArray results)
            throws RelutionTagInfoRegistryNotAvailable {
        Map<Long, RelutionTagInfo> mapping = new HashMap<>();
        try {
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                long id = result.getLong("id");
                String name = result.getString("name");
                String description = result.getString("description");
                RelutionTagInfo relutionTagInfo = new RelutionTagInfo();
                relutionTagInfo.setId(id);
                relutionTagInfo.setName(name);
                relutionTagInfo.setDescription(description);
                mapping.put(id, relutionTagInfo);
            }
        } catch (JSONException e) {
            throw new RelutionTagInfoRegistryNotAvailable();
        }
        return mapping;
    }

    @Override
    public void stopUpdatingRegistry() {
        this.thread.interrupt();
    }

    @Override
    public RelutionTagInfo getRelutionTagInfoForTag(long tag) throws RelutionTagInfoRegistryNoInfoFound{
        RelutionTagInfo relutionTagInfo = tagToInfo.get(tag);
        if (relutionTagInfo == null) {
            throw new RelutionTagInfoRegistryNoInfoFound();
        }
        return relutionTagInfo;
    }

    public long getWaitTimeBetweenRelutionTagRegistrySynchronizationInMs() {
        return waitTimeBetweenRelutionTagRegistrySynchronizationInMs;
    }

    public void setWaitTimeBetweenRelutionTagRegistrySynchronizationInMs(
            long waitTimeBetweenRelutionTagRegistrySynchronizationInMs) {
        this.waitTimeBetweenRelutionTagRegistrySynchronizationInMs =
                waitTimeBetweenRelutionTagRegistrySynchronizationInMs;
    }
}
