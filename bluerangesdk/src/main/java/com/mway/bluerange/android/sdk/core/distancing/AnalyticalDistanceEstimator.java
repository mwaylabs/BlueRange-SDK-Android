//
//  AnalyticalDistanceEstimator.java
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
