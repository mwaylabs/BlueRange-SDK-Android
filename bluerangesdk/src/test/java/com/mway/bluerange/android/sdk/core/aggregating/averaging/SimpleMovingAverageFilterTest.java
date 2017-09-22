//
//  SimpleMovingAverageFilterTest.java
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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SimpleMovingAverageFilterTest {

    private SimpleMovingAverageFilter filter;

    @Before
    public void setUp() {
        this.filter = new SimpleMovingAverageFilter();
    }

    @Test
    public void testAverageWithOneValue() {
        float expectedValue = 10f;

        long minTime = 0;
        long maxTime = 1;
        List<Long> timePoints = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        timePoints.add(0l);
        values.add(expectedValue);

        float actualValue = filter.getAverage(minTime, maxTime, timePoints, values);

        Assert.assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testAverageWithTwoValues() {
        long minTime = 0;
        long maxTime = 1;
        List<Long> timePoints = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        timePoints.add(0l);
        values.add(1.0f);
        timePoints.add(1l);
        values.add(2.0f);

        float actualValue = filter.getAverage(minTime, maxTime, timePoints, values);

        Assert.assertEquals(1.5f, actualValue);
    }
}
