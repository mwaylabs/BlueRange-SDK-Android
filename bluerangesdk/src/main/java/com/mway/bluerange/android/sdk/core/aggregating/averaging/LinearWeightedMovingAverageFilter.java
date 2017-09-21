//
//  LinearWeightedMovingAverageFilter.java
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

package com.mway.bluerange.android.sdk.core.aggregating.averaging;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the {@link MovingAverageFilter} interface. The
 * average value is computed by linearly weighting all values, whereas
 * new values will have a greater weight than older values. The oldest
 * value defined at f(minTime) will be weighted with the constant c.
 */
public class LinearWeightedMovingAverageFilter implements MovingAverageFilter {

    private float c;

    public LinearWeightedMovingAverageFilter(float c) {
        this.c = c;
    }

    @Override
    public float getAverage(long startTime, long endTime, List<Long> timePoints, List<Float> values) {
        // 1. If list consists only of one value, take this value as average
        if (values.size() == 1) {
            return values.get(0);
        }

        // 1. Compute weights
        List<Float> weights = new ArrayList<>();
        float sumWeights = 0f;
        for (int i = 0; i < timePoints.size(); i++) {
            float timePoint = timePoints.get(i);
            float relativePosition = (timePoint- startTime)/(endTime - startTime);
            float m = 1.0f-c;
            float weight = m*relativePosition + c;
            weights.add(weight);
            sumWeights += weight;
        }
        // 2. Compute the values' average by summing up all values weighted by the normalized weights.
        // Normalization means: The sum of all weights must be 1.
        float average = 0f;
        for (int i = 0; i < values.size(); i++) {
            average += (weights.get(i)/sumWeights) * values.get(i);
        }
        return average;
    }
}
