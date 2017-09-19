//
//  RelutionImpl.java
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

package com.mway.bluerange.android.sdk.services.relution;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;

import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionInformation;
import com.mway.bluerange.android.sdk.services.relution.filter.Filter;
import com.mway.bluerange.android.sdk.services.relution.filter.LogOpFilter;
import com.mway.bluerange.android.sdk.services.relution.filter.LongFilter;
import com.mway.bluerange.android.sdk.services.relution.filter.StringFilter;
import com.mway.bluerange.android.sdk.services.relution.model.AdvertisingMessagesConfiguration;
import com.mway.bluerange.android.sdk.services.relution.model.RelutionTagInfos;
import com.mway.bluerange.android.sdk.utils.network.HttpClient;
import com.mway.bluerange.android.sdk.utils.network.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.client.utils.URIBuilder;

public class RelutionImpl implements Relution {

    // Android
    private Context context;

    // Configuration
    private String baseUrl;
    private String username;
    private String password;

    // Http Client
    private HttpClient httpClient;

    // State
    private String organizationUuid;
    private String userUuid;

    @Override
    public boolean isServerAvailable() {
        return Network.isServerAvailable(context, this.baseUrl);
    }

    public RelutionImpl(Context context, String baseUrl, String username, String password)
            throws RelutionException, LoginException {
        this.context = context;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.httpClient = new HttpClient(context);
        login();
    }

    private void login() throws RelutionException, LoginException {
        try {
            JSONObject loginJson = new JSONObject();
            loginJson.put("orgaName", JSONObject.NULL);
            loginJson.put("userName", this.username);
            loginJson.put("password", this.password);
            loginJson.put("email", JSONObject.NULL);
            HttpClient.JsonResponse response = httpClient.postWithResponseData(
                    this.baseUrl + "/gofer/security/rest/auth/login",
                    loginJson
            );
            if (isSuccessStatusCode(response.statusCode)) {
                String jsonString = new String(response.responseBody);
                JSONObject jsonResponse = new JSONObject(jsonString);
                JSONObject user = jsonResponse.getJSONObject("user");
                this.organizationUuid = user.getString("organizationUuid");
                this.userUuid = user.getString("uuid");
            } else if (response.statusCode == 401) {
                throw new LoginException();
            } else {
                throw new RelutionException();
            }
        } catch (LoginException e) {
            throw e;
        } catch (Throwable t) {
            throw new RelutionException();
        }
    }

    @Override
    public AdvertisingMessagesConfiguration getAdvertisingMessagesConfiguration() throws Exception {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Gofer-User", this.userUuid);

            String url = this.baseUrl + "/relution/api/v1/iot/advertisingMessages/configuration/"
                    + this.organizationUuid;
            JSONObject responseObject = httpClient.get(url);

            verifyRelutionStatus(responseObject);
            JSONArray results = responseObject.getJSONArray("results");
            AdvertisingMessagesConfiguration configuration = new AdvertisingMessagesConfiguration(results);
            return configuration;
        } catch (Throwable t) {
            throw new Exception("Requesting sites failed. " + t.getMessage());
        }
    }

    @Override
    public void sendAnalyticsReport(JSONObject jsonReport) throws Exception {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Gofer-User", this.userUuid);
            headers.put("Authorization", "Basic c29mb2JvZGVqZXNv");

            String url = this.baseUrl + "/relution/api/v1/iot/analytics/raw";
            JSONObject responseObject = httpClient.post(url, jsonReport, headers);

            verifyRelutionStatus(responseObject);
        } catch (Throwable t) {
            throw new Exception("Sending analytics report failed. " + t.getMessage());
        }
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void setCalibratedRssiForIBeacon(IBeacon iBeacon, int calibratedRssi) throws Exception {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Gofer-User", this.userUuid);
            String encodedAuthorizationString = getEncodedAuthorizationString(username, password);
            headers.put("Authorization", "Basic " + encodedAuthorizationString);

            String body = "" + calibratedRssi;

            Filter filter = new LogOpFilter(LogOpFilter.Operation.AND,
                    new StringFilter("type", "IBEACON"),
                    new StringFilter("beaconUuid", iBeacon.getUuid().toString().toUpperCase()),
                    new LongFilter("major", iBeacon.getMajor()),
                    new LongFilter("minor", iBeacon.getMinor()));

            String burl = this.baseUrl
                    + "/relution/api/v1/iot/advertisingMessages/calibrateBeaconRssi?filter="
                    + filter.toString();

            JSONObject responseObject = httpClient.put(burl, body, headers);

            verifyRelutionStatus(responseObject);
        } catch (Throwable t) {
            throw new Exception("Sending calibrated RSSI failed. " + t.getMessage());
        }
    }

    private String getEncodedAuthorizationString(String username, String password) {
        String authorizationString = username + ":" + password;
        String encodedAuthorizationString = Base64.encodeToString(
                authorizationString.getBytes(), Base64.NO_WRAP);
        return encodedAuthorizationString;
    }

    @Override
    public RelutionActionInformation getActionsForIBeacon(IBeacon iBeacon) throws Exception {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Gofer-User", this.userUuid);

            String url = getUrlForIBeaconActionMapping(iBeacon).toString();
            JSONObject responseObject = httpClient.get(url, headers);

            verifyRelutionStatus(responseObject);

            RelutionActionInformation actionInformation = new RelutionActionInformation();
            actionInformation.setInformation(responseObject);
            return actionInformation;
        } catch (Throwable t) {
            throw new Exception("Getting actions for iBeacon failed. " + t.getMessage());
        }
    }

    private URL getUrlForIBeaconActionMapping(IBeacon iBeacon) {
        // Request has the following form:
        /* /campaigns/sdk
            ?filter=
                {
                    "type": "logOp",
                    "operation": "AND",
                    "filters": [{
                        "type": "string",
                        "fieldName": "devices.organizationUuid",
                        "value": "65AC11A8-99BE-45BB-80A0-F8C51FF6476F"
                    }, {
                        "type": "string",
                        "fieldName": "devices.advertisingMessages.ibeaconUuid",
                        "value": "B9407F30-F5F8-466E-AFF9-25556B57FE6D"
                    }, {
                        "type": "string",
                        "fieldName": "devices.advertisingMessages.ibeaconMajor",
                        "value": "1"
                    }, {
                        "type": "string",
                        "fieldName": "devices.advertisingMessages.ibeaconMinor",
                        "value": "1"
                    }]
                }*/
        URL url = null;
        try {
            String baseEndpointUrl = this.baseUrl + "/relution/api/v1/iot/campaigns/actions";
            URIBuilder b = new URIBuilder(baseEndpointUrl);
            String filterString = getFilterStringForIBeacon(iBeacon);
            b.addParameter("filter", filterString);
            url = b.build().toURL();
        } catch (URISyntaxException e) {
            // This catch block should not be reached normally.
            // If it does, something is wrong! Terminate the application.
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // This catch block should not be reached normally.
            // If it does, something is wrong! Terminate the application.
            e.printStackTrace();
        }
        return url;
    }

    @SuppressLint("DefaultLocale")
    private String getFilterStringForIBeacon(IBeacon iBeacon) {
        JSONObject filterObject = new JSONObject();
        try {
            filterObject.put("type", "logOp");
            filterObject.put("operation", "AND");
            JSONArray filtersArray = new JSONArray();
            JSONObject organisationUuidObject = new JSONObject();
            organisationUuidObject.put("type", "string");
            organisationUuidObject.put("fieldName", "devices.organizationUuid");
            organisationUuidObject.put("value", this.organizationUuid.toUpperCase());
            JSONObject iBeaconUuidObject = new JSONObject();
            iBeaconUuidObject.put("type", "string");
            iBeaconUuidObject.put("fieldName", "devices.advertisingMessages.beaconUuid");
            iBeaconUuidObject.put("value", iBeacon.getUuid().toString());
            JSONObject iBeaconMajorObject = new JSONObject();
            iBeaconMajorObject.put("type", "string");
            iBeaconMajorObject.put("fieldName", "devices.advertisingMessages.major");
            iBeaconMajorObject.put("value", "" + iBeacon.getMajor());
            JSONObject iBeaconMinorObject = new JSONObject();
            iBeaconMinorObject.put("type", "string");
            iBeaconMinorObject.put("fieldName", "devices.advertisingMessages.minor");
            iBeaconMinorObject.put("value", "" + iBeacon.getMinor());
            filtersArray.put(organisationUuidObject);
            filtersArray.put(iBeaconUuidObject);
            filtersArray.put(iBeaconMajorObject);
            filtersArray.put(iBeaconMinorObject);
            filterObject.put("filters", filtersArray);
        } catch (JSONException e) {
            // Should not be reached!
        }
        return filterObject.toString();
    }

    @Override
    public RelutionTagInfos getRelutionTagInfos() throws Exception {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Gofer-User", this.userUuid);
            headers.put("Authorization", "Basic c29mb2JvZGVqZXNv");

            Filter filter = new StringFilter("organizationUuid", this.organizationUuid);
            String url = this.baseUrl + "/relution/api/v1/tags" + "?filter=" + filter;
            JSONObject responseObject = httpClient.get(url, headers);

            verifyRelutionStatus(responseObject);

            JSONArray results = responseObject.getJSONArray("results");
            RelutionTagInfos relutionTagInfos = new RelutionTagInfos(results);
            return relutionTagInfos;
        } catch (Throwable t) {
            throw new Exception("Getting Relution Tag infos failed. " + t.getMessage());
        }
    }

    private void verifyRelutionStatus(JSONObject responseObject) throws Exception {
        int status = responseObject.getInt("status");
        if (status != 0) {
            String exceptionMessage = "Relution status = " + status;
            if (responseObject.has("message")) {
                exceptionMessage += ", message = " + responseObject.getString("message");
            }
            throw new Exception(exceptionMessage);
        }
    }

    @Override
    public String getOrganizationUuid() {
        return this.organizationUuid;
    }

    private boolean isSuccessStatusCode(int statusCode) {
        return ((statusCode >= 200) && (statusCode <= 299));
    }
}
