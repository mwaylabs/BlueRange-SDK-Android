//
//  SerialNumberUtils.java
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
