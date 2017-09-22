//
//  BeaconMessageReportSender.java
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

/**
 * A beacon message report sender is used by a {@link BeaconMessageReporter} instance to
 * periodically send reports to a receiver. Implementations of this interface might deliver the
 * report to a webservice or to another node in the message processing graph.
 */
public interface BeaconMessageReportSender {

    class SendReportException extends Exception{}
    class UnresolvableSendReportException extends Exception{}

    boolean receiverAvailable();
    void sendReport(BeaconMessageReport report) throws SendReportException, UnresolvableSendReportException;
}
