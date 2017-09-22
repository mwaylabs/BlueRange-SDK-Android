//
//  BeaconMessageReportBuilder.java
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

package com.mway.bluerange.android.sdk.core.reporting;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

/**
 * An interface specifying a builder that constructs report from a stream of beacon messages
 * delivered by subsequently calling the {@link #addBeaconMessage} method.
 */
public interface BeaconMessageReportBuilder {

    class BuildException extends Exception {}
    class NoMessagesException extends Exception {}

    /**
     * Starts with a new report
     * @throws BuildException if an error occurred.
     */
    void newReport() throws BuildException;

    /**
     * A builder method that commands the builder to add the beacon message to the report.
     * @param message the message to be added.
     * @throws BuildException will be thrown, if an error occurred.
     */
    void addBeaconMessage(BeaconMessage message) throws BuildException;

    /**
     * Returns a newly constructed report containing all added messages.
     * @return the newly consturcted beacon message report.
     * @throws BuildException will be thrown, if an error occurred.
     */
    BeaconMessageReport buildReport()  throws BuildException;
}
