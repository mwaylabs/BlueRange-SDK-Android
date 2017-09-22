//
//  BeaconMessagePersistor.java
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

package com.mway.bluerange.android.sdk.core.logging;

import java.util.Iterator;

import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

/**
 * A beacon message persistor saves all messages persistently that are passed to the {@link
 * #writeMessage}. method. The {@link #clearMessages} method deletes the complete log. The log
 * iterator returned by the {@link #getLogIterator} method makes it possible to iteratively
 * access all beacon messages of the log. The log iterator should be implemented in a thread-safe
 * manner and should consider changes during iteration.
 */
public interface BeaconMessagePersistor {
    // Read operations
    BeaconMessageLog readLog();
    Iterator<BeaconMessage> getLogIterator();
    int getTotalMessages();

    // Write operations
    void writeMessage(BeaconMessage beaconMessage);
    void clearMessages();

    class BeaconMessageWriteFailedException extends Exception {}
}
