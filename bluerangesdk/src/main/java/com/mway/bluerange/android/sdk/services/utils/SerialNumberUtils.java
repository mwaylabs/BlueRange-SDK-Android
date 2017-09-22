//
//  SerialNumberUtils.java
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

package com.mway.bluerange.android.sdk.services.utils;

public class SerialNumberUtils {
    public static final String ALPHABET = "BCDFGHJKLMNPQRSTVWXYZ123456789";

    public static String generateBeaconSerialForIndex(long index){
        String serial = "";

        while(serial.length() < 5){
            int rest = (int)(index % ALPHABET.length());
            serial += ALPHABET.substring(rest, rest+1);
            index /= ALPHABET.length();
        }

        return new StringBuilder(serial).reverse().toString();
    }

    public static String generateAssetSerialForIndex(long index){
        String serial = generateBeaconSerialForIndex(index);

        if(index < 0 || serial.charAt(0) != 'B' ){
            throw new RuntimeException("Index out of range");
        }

        return "A" + serial.substring(1, 5);
    }

    public static long getIndexForSerial(String serialNumber){
        if(serialNumber.charAt(0) == 'A') serialNumber = serialNumber.substring(1, 5);

        int index = 0;
        for(int i=0; i<serialNumber.length(); i++){
            char currentChar = serialNumber.charAt(serialNumber.length()-i-1);
            int charValue = ALPHABET.indexOf(currentChar);
            index += Math.pow(ALPHABET.length(), i) * charValue;
        }
        return index;
    }

    // For legacy reason, the nodeId was always the serialNumberIndex + 50
    // Because older test beacons were given manual ids from 1 - 50.
    public static String generateBeaconSerialForNodeId(long nodeId){
        return generateBeaconSerialForIndex(nodeId - 50);
    }

    public static long getNodeIdForSerial(String serialNumber){
        return getIndexForSerial(serialNumber) + 50;
    }
}
