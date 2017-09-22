//
//  AnalyticalDistanceEstimator.java
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
 * This class implements an analytical distance estimator based on the path-loss formula as
 * described in: http://electronics.stackexchange
 * .com/questions/83354/calculate-distance-from-rssi or Y. Wang, X. Yang, Y. Zhao, Y. Liu, L.
 * Cuthbert, Bluetooth positioning using rssi and triangulation methods, in: Consumer
 * Communications and Networking Conference (CCNC), 2013 IEEE, 2013, pp. 837{842.
 */
public class AnalyticalDistanceEstimator implements DistanceEstimator {

    // A is the received signal strength in dBm at 1 metre.
    // We set A as described in:
    // http://stackoverflow.com/questions/30177965/rssi-to-distance-with-beacons
    public static final float kDefaultA = -54;
    private float A = kDefaultA;

    // n is the propagation constant or path-loss exponent
    // (Free space has n =2 for reference)
    // Typically n is in the range of [0,2].
    public static final float kDefaultN = 2;
    private float n = kDefaultN;

    @Override
    public float getDistanceInMetres(float rssi, float txPower) {
        return rssiToDistanceWithA(rssi, txPower);
    }

    public float rssiToDistance(float rssi) {
        return rssiToDistanceWithNAndA(rssi, n, A);
    }

    public float rssiToDistanceWithN(float rssi, float n) {
        return rssiToDistanceWithNAndA(rssi, n, A);
    }

    public float rssiToDistanceWithA(float rssi, float A) {
        return rssiToDistanceWithNAndA(rssi, n, A);
    }

    public float rssiToDistanceWithNAndA(float rssi, float n, float A) {
        float distanceInMetre = (float)(Math.pow(10, (A-rssi)/(10*n)));
        return distanceInMetre;
    }

    public float distanceToRssi(float distanceInMetre) {
        return distanceToRssiWithNAndA(distanceInMetre, n, A);
    }

    public float distanceToRssiWithN(float distanceInMetre, float n) {
        return distanceToRssiWithNAndA(distanceInMetre, n, A);
    }

    public float distanceToRssiWithA(float distanceInMetre, float A) {
        return distanceToRssiWithNAndA(distanceInMetre, n, A);
    }

    public float distanceToRssiWithNAndA(float distanceInMetre, float n, float A) {
        float d = distanceInMetre;
        float rssi = (float)(-10*n * Math.log10(d)+A);
        return rssi;
    }

    public float getPropagationConstantFromRSSIAndDistance(float rssi, float distance) {
        float n = (float)((rssi - getA())/(-10 * Math.log10(distance)));
        return n;
    }

    public float getA() {
        return A;
    }

    public void setA(float a) {
        A = a;
    }

    public float getN() {
        return n;
    }

    public void setN(float n) {
        this.n = n;
    }
}
