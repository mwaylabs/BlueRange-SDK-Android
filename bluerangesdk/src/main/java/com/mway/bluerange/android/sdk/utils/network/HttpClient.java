//
//  HttpClient.java
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

package com.mway.bluerange.android.sdk.utils.network;

import android.annotation.SuppressLint;
import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.mway.bluerange.android.sdk.common.Constants;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HttpClient {

    // Android context
    private Context context;

    // Client
    private int timeoutInMs = 5 * 1000;
    private SyncHttpClient client;

    // Classes
    public class RequestException extends Exception {
        private static final long serialVersionUID = 1L;
        public RequestException(String message) {
            super(message);
        }
    }

    public static class Parameter {
        public String key;
        public String value;
        public Parameter(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static final int DEFAULT_STATUS_CODE = -1;
    public static class JsonResponse {
        public int statusCode = DEFAULT_STATUS_CODE;
        public byte[] responseBody;
        public Header[] headers;
    }

    public HttpClient(Context context) {
        this.context = context;
        this.client = new SyncHttpClient();
        if (Constants.mode == Constants.Mode.DEVELOPMENT_MODE) {
            DevelopmentModeSSLSocketFactory socketFactory = trustAllCertificates();
            this.client.setSSLSocketFactory(socketFactory);
        }
        this.client.setMaxRetriesAndTimeout(0, timeoutInMs);
        this.client.setTimeout(timeoutInMs);
    }

    private class DevelopmentModeSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        @SuppressLint("TrustAllX509TrustManager")
        public DevelopmentModeSSLSocketFactory(KeyStore truststore)
                throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    private DevelopmentModeSSLSocketFactory trustAllCertificates() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            DevelopmentModeSSLSocketFactory sf = new DevelopmentModeSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(DevelopmentModeSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            return sf;
        } catch (Exception e) {
        }
        return null;
    }

    public JSONObject get(String url) throws RequestException {
        return get(url, new ArrayList<Parameter>());
    }

    public JSONObject get(String url, List<Parameter> parameters) throws RequestException {
        return get(url, new HashMap<String, String>(), parameters);
    }

    public JSONObject get(String url, Map<String, String> headers) throws RequestException {
        return get(url, headers, new ArrayList<Parameter>());
    }

    public JSONObject get(String url, Map<String, String> headers, List<Parameter> parameters)
            throws RequestException {
        try {
            Map<String, Object> parameterList = new HashMap<String, Object>();
            for (Parameter parameter : parameters) {
                parameterList.put(parameter.key, parameter.value);
            }
            client.addHeader("Content-Type", "application/json");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                client.addHeader(key, value);
            }
            RequestParams params = new RequestParams();
            for (Parameter parameter : parameters) {
                params.put(parameter.key, parameter.value);
            }
            final JsonResponse response = new JsonResponse();
            client.get(context, url, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    response.statusCode = statusCode;
                    response.headers = headers;
                    response.responseBody = responseBody;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    response.statusCode = statusCode;
                    response.headers = headers;
                    response.responseBody = responseBody;
                }
            });
            JSONObject jsonObject = new JSONObject(new String(response.responseBody));
            return jsonObject;
        } catch (Throwable t) {
            throw new RequestException("Error with GET " + url + ". " + t.getMessage());
        }
    }

    public JSONObject post(final String url, final JSONObject jsonObject) throws RequestException {
        return post(url, jsonObject, new HashMap<String, String>());
    }

    public JSONObject post(final String url, final JSONObject jsonObject, Map<String, String> headers)
            throws RequestException {
        try {
            JsonResponse response = postWithResponseData(url, jsonObject, headers);
            JSONObject result = new JSONObject(new String(response.responseBody));
            return result;
        } catch (Exception e) {
            throw new RequestException("Error with POST " + url + ". " + e.getMessage());
        }
    }

    public JsonResponse postWithResponseData(final String url, final JSONObject jsonObject) throws RequestException {
        return postWithResponseData(url, jsonObject, new HashMap<String, String>());
    }

    public JsonResponse postWithResponseData(final String url, final JSONObject jsonObject, Map<String, String> headers)
            throws RequestException {
        try {
            // Headers
            client.addHeader("Content-Type", "application/json");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                client.addHeader(key, value);
            }
            // Body
            StringEntity entity = createEntity(jsonObject.toString());

            // Request
            final JsonResponse response = new JsonResponse();
            client.post(context, url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    response.statusCode = statusCode;
                    response.headers = headers;
                    response.responseBody = responseBody;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                      Throwable error) {
                    response.statusCode = statusCode;
                    response.headers = headers;
                    response.responseBody = responseBody;
                }
            });
            return response;
        } catch (Exception e) {
            throw new RequestException("Error with POST " + url + ". " + e.getMessage());
        }
    }

    public JSONObject put(String url, JSONObject jsonObject, Map<String, String> headers) throws RequestException {
        return put(url, jsonObject.toString(), headers);
    }

    public JSONObject put(String url, String body, Map<String, String> headers) throws RequestException {
        try {
            JsonResponse response = putWithResponseData(url, body, headers);
            JSONObject result = new JSONObject(new String(response.responseBody));
            return result;
        } catch (Exception e) {
            throw new RequestException("Error with PUT " + url + ". " + e.getMessage());
        }
    }

    public JsonResponse putWithResponseData(final String url, String body, Map<String, String> headers)
            throws RequestException {
        try {
            // Headers
            client.addHeader("Content-Type", "application/json");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                client.addHeader(key, value);
            }
            // Body
            final StringEntity entity = createEntity(body);

            // Request
            final JsonResponse response = new JsonResponse();

            // Request only works when send on a separate thread.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.put(context, url, entity, "application/json", new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            response.statusCode = statusCode;
                            response.headers = headers;
                            response.responseBody = responseBody;
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                              Throwable error) {
                            response.statusCode = statusCode;
                            response.headers = headers;
                            response.responseBody = responseBody;
                        }
                    });
                }
            }).start();

            return response;
        } catch (Exception e) {
            throw new RequestException("Error with PUT " + url + ". " + e.getMessage());
        }
    }

    private StringEntity createEntity(String content) {
        StringEntity entity = null;
        try {
            entity = new StringEntity(content);
        } catch (UnsupportedEncodingException e) {
            // Do nothing, just return null.
        }
        return entity;
    }

}
