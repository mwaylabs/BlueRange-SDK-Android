//
//  ByteArrayParser.java
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
