//
//  LinearWeightedMovingAverageFilter.java
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
