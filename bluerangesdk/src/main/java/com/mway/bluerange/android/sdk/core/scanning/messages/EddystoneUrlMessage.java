//
//  EddystoneUrlMessage.java
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

package com.mway.bluerange.android.sdk.core.scanning.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

import org.altbeacon.beacon.Beacon;

import java.io.UnsupportedEncodingException;

/**
 * Represents an Eddystone URL message.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("EddystoneUrlMessage")
public class EddystoneUrlMessage extends EddystoneMessage {

    private String url;

    private static URLScheme[] urlSchemes = new URLScheme[]{
            new URLScheme(0x00, "http://www."),
            new URLScheme(0x01, "https://www."),
            new URLScheme(0x02, "http://"),
            new URLScheme(0x03, "https://")
    };
    private static StringExpander[] urlExpanders = new StringExpander[]{
            new StringExpander(0x00, ".com/"),
            new StringExpander(0x01, ".org/"),
            new StringExpander(0x02, ".edu/"),
            new StringExpander(0x03, ".net/"),
            new StringExpander(0x04, ".info/"),
            new StringExpander(0x05, ".biz/"),
            new StringExpander(0x06, ".gov/"),
            new StringExpander(0x07, ".com"),
            new StringExpander(0x08, ".org"),
            new StringExpander(0x09, ".edu"),
            new StringExpander(0x0a, ".net"),
            new StringExpander(0x0b, ".info"),
            new StringExpander(0x0c, ".biz"),
            new StringExpander(0x0d, ".gov"),
    };

    private static class URLScheme {
        public int code;
        public String expansion;

        public URLScheme(int code, String expansion) {
            this.code = code;
            this.expansion = expansion;
        }
    }
    private static class StringExpander {
        public int asciiCode;
        public String expansion;

        public StringExpander(int asciiCode, String expansion) {
            this.asciiCode = asciiCode;
            this.expansion = expansion;
        }
    }
    public static class WrongUrlFormatException extends RuntimeException {
        public WrongUrlFormatException(String message) {
            super(message);
        }
    }

    // Default constructor necessary for JSON deserialization
    public EddystoneUrlMessage() {}

    public EddystoneUrlMessage(Beacon beacon) {
        super(beacon);
        this.url = getUrlStringFromBytes(beacon.getId1().toByteArray());
    }

    public EddystoneUrlMessage(String url) {
        super();
        this.url = url;
    }

    /**
     * Returns the Eddystone url string based on the byte array
     * as it is encoded in the Eddystone URL message. The byte array
     * must be ordered in big endian.
     * @param bytes the byte array as specified in the Eddystone URL message.
     * @return the URL that is encoded in the byte array.
     */
    protected static String getUrlStringFromBytes(byte[] bytes) {
        String urlScheme = getUrlScheme(bytes);
        String urlTail = getUrlRemainingPart(bytes);
        return urlScheme + urlTail;
    }

    private static String getUrlScheme(byte[] bytes) {
        byte b = bytes[0];
        for (URLScheme urlScheme : urlSchemes) {
            if (urlScheme.code == b) {
                return urlScheme.expansion;
            }
        }
        throw new WrongUrlFormatException("URL Scheme is wrong!");
    }

    private static String getUrlRemainingPart(byte[] bytes) {
        String result = "";
        for (int i = 1; i < bytes.length; i++) {
            byte b = bytes[i];
            String expander = getStringExpanderForByte(b);
            if (expander != null) {
                result += expander;
            } else {
                byte[] thisbyte = new byte[1];
                thisbyte[0] = b;
                try {
                    result += new String(thisbyte, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static String getStringExpanderForByte(byte b) {
        for (StringExpander stringExpander : urlExpanders) {
            if (b == stringExpander.asciiCode) {
                return stringExpander.expansion;
            }
        }
        return null;
    }

    /**
     * Returns a byte array of the url string in big endian order.
     * @param urlString The eddystone url.
     * @return byte array as it is specified in the Eddystone format.
     * @throws WrongUrlFormatException will be thrown, if the URL is illegal.
     */
    protected static byte[] getUrlBytesFromString(String urlString) throws WrongUrlFormatException {

        // 17 is the maximum size + 1 for the url scheme
        final int maxSize = 17;
        byte[] tmp = new byte[maxSize];
        URLScheme urlScheme = null;

        // Url scheme
        for(int i = 0; i < urlSchemes.length; i++){
            if(urlString.startsWith(urlSchemes[i].expansion)){
                urlScheme = urlSchemes[i];
                break;
            }
        }

        if(urlScheme == null) {
            throw new WrongUrlFormatException("URL has wrong format");
        }

        // Strip urlScheme from url
        urlString = urlString.substring(urlScheme.expansion.length());

        // Encode the rest of the url with expanders and bytes
        int pos = 0;

        while(urlString.length() > 0){
            if(pos >= maxSize) {
                throw new WrongUrlFormatException("URL is too long to be encoded");
            }

            Boolean found = false;
            //Check if current url starts with one of the expanders
            for (StringExpander urlExpander : urlExpanders) {
                if (urlString.startsWith(urlExpander.expansion)) {
                    tmp[pos] = (byte) urlExpander.asciiCode;
                    urlString = urlString.substring(urlExpander.expansion.length());
                    found = true;
                    break;
                }
            }
            //If the url does not begin with one of the expanders, we write one character
            if(!found){
                tmp[pos] = (byte) urlString.getBytes()[0];
                urlString = urlString.substring(1);
            }
            pos++;
        }

        // Fill the remaining bytes with padding
        int numRemainingBytes = tmp.length - pos;
        for (int i = 0; i < numRemainingBytes; i++) {
            tmp[pos+i] = 0;
        }

        byte[] result = new byte[pos+1];
        result[0] = (byte)urlScheme.code;
        for(int i = 0; i < pos; i++){
            result[i+1] = tmp[i];
        }

        return result;
    }

    @Override
    protected BeaconMessage copy() {
        return new EddystoneUrlMessage(url);
    }

    @Override
    public int hashCode() {
        return this.url.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EddystoneUrlMessage)) {
            return false;
        }
        EddystoneUrlMessage beaconMessage = (EddystoneUrlMessage) o;
        return beaconMessage.getUrl().equals(this.getUrl());
    }

    @Override
    protected String getDescription() {
        return "Eddystone URL: " + this.url;
    }

    // Getters and setters

    /**
     * Returns the URL of the Eddystone URL message. E.g. "https://goo.gl/Aq18zF"
     * @return The URL contained in the Eddystone URL message.
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
