//
//  ByteArrayParser.java
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

package com.mway.bluerange.android.sdk.utils.structs;

public class ByteArrayParser {

    private int pointer = 0;

    public ByteArrayParser(int offset) {
        pointer = offset;
    }

    public int readUnsignedShort(byte[] bytes) {
        int s = ((short)(bytes[pointer] << 8)) + (short)((bytes[pointer+1] << 0));
        pointer += 2;
        return s;
    }

    public int readSwappedUnsignedShort(byte[] bytes) {
        int i = readSwappedUnsignedShort(bytes, pointer);
        pointer += 2;
        return i;
    }

    public long readSwappedUnsignedInteger(byte[] bytes) {
        long l = readSwappedUnsignedInteger(bytes, pointer);
        pointer += 4;
        return l;
    }

    public short readSwappedShort(byte[] bytes) {
        short s = readSwappedShort(bytes, pointer);
        pointer += 2;
        return s;
    }

    public short readSwappedBitsOfByteWithLockedPointer(byte[] bytes, int startBit, int endBit) {
        final int BYTE_LENGTH = 8;
        byte mask = 0;
        for (int i = 0; i < BYTE_LENGTH; i++) {
            if (i >= startBit && i <= endBit) {
                mask += Math.pow(2,i);
            }
        }
        short s = (short)(((bytes[pointer] & mask)) >> startBit);
        return s;
    }

    public short readSwappedBitsOfByte(byte[] bytes, int startBit, int endBit) {
        short s = readSwappedBitsOfByteWithLockedPointer(bytes, startBit, endBit);
        pointer += 1;
        return s;
    }

    public short readSwappedUnsignedByte(byte[] bytes) {
        short s = (short)(((short)bytes[pointer]) & 0xff);
        pointer += 1;
        return s;
    }

    public short readSwappedByte(byte[] bytes) {
        short s = (short)(bytes[pointer]);
        pointer += 1;
        return s;
    }

    private int readSwappedUnsignedShort(byte[] data, int offset) {
        return ( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
                ( ( data[ offset + 1 ] & 0xff ) << 8 ) );
    }

    private long readSwappedUnsignedInteger(byte[] data, int offset) {
        long low = ( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
                ( ( data[ offset + 1 ] & 0xff ) << 8 ) +
                ( ( data[ offset + 2 ] & 0xff ) << 16 ) );

        long high = data[ offset + 3 ] & 0xff;

        return (high << 24) + (0xffffffffL & low);
    }

    private short readSwappedShort(byte[] data, int offset) {
        return (short)( ( ( data[ offset + 0 ] & 0xff ) << 0 ) +
                ( ( data[ offset + 1 ] & 0xff ) << 8 ) );
    }
}
