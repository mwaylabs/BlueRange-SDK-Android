//
//  EmpiricalDistanceEstimator.java
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

package com.mway.bluerange.android.sdk.core.distancing;

/**
 * This class implements an distance estimator based on empirical data as described in
 * http://developer.radiusnetworks.com/2014/12/04/fundamentals-of-beacon-ranging.html.
 */
public class EmpiricalDistanceEstimator implements DistanceEstimator {

    @Override
    public float getDistanceInMetres(float rssi, float txPower) {
        if (rssi == 0) {
            return -1.0f; // if we cannot determine distance, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return (float)(Math.pow(ratio,10));
        }
        else {
            double distance =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return (float)distance;
        }
    }
}
