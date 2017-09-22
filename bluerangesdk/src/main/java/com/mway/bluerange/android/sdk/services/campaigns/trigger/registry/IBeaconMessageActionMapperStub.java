//
//  IBeaconMessageActionMapperStub.java
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

package com.mway.bluerange.android.sdk.services.campaigns.trigger.registry;

import com.mway.bluerange.android.sdk.core.scanning.messages.IBeacon;
import com.mway.bluerange.android.sdk.core.scanning.messages.IBeaconMessage;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionInformation;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionCampaign;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.RelutionActionRegistry;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.content.RelutionContentAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.notification.RelutionNotificationAction;
import com.mway.bluerange.android.sdk.services.campaigns.trigger.actions.tag.RelutionTagAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class IBeaconMessageActionMapperStub extends BeaconMessageActionMapperStub implements IBeaconMessageActionMapper {

    private Map<IBeacon, JSONObject> iBeaconActionMap = new HashMap<>();
    private boolean corruptJsonResponses = false;

    public IBeaconMessageActionMapperStub() {
        int major = 45;
        int minor = 1;
        try {
            addContentActionNotExpired(new IBeacon(UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addNotificationAction(new IBeacon(UUID.fromString("c9407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithExpiredValidity(new IBeacon(UUID.fromString("d9407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithDelayedValidity(new IBeacon(UUID.fromString("e9407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithMissingParameters(new IBeacon(UUID.fromString("f9407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithHighRepeatEveryParameter(new IBeacon(UUID.fromString("09407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addDelayedNotificationAction(new IBeacon(UUID.fromString("19407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addLockedNotificationAction(new IBeacon(UUID.fromString("29407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithLowRepeatEveryParameter(new IBeacon(UUID.fromString("39407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithHighDistanceThreshold(new IBeacon(UUID.fromString("49407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithNoSpecifiedDistanceThreshold(new IBeacon(UUID.fromString("59407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addTagActionWithCheckoutTag(new IBeacon(UUID.fromString("69407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addTagActionWithFoyerTag(new IBeacon(UUID.fromString("79407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithExpiredCampaign(new IBeacon(UUID.fromString("89407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithInactiveCampaign(new IBeacon(UUID.fromString("99407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addTwoContentActionsInDifferentCampaigns(new IBeacon(UUID.fromString("91407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addTagActionWithFruitsAndVegetablesTag(new IBeacon(UUID.fromString("92407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithHTMLContent(new IBeacon(UUID.fromString("93407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addNotificationActionWithHighDistanceThreshold(new IBeacon(UUID.fromString("94407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithLongDelayedValidity(new IBeacon(UUID.fromString("95407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
            addContentActionWithLockAndDelay(new IBeacon(UUID.fromString("96407f30-f5f8-466e-aff9-25556b57fe6d"), major, minor));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RelutionActionInformation getBeaconActionInformation(IBeaconMessage message) throws RelutionActionRegistry.UnsupportedMessageException {
        RelutionActionInformation actionInformation = new RelutionActionInformation();
        IBeacon iBeacon = message.getIBeacon();
        JSONObject jsonObject = iBeaconActionMap.get(iBeacon);
        // If iBeacon does not exist just throw exception
        if (jsonObject == null) {
            throw new RelutionActionRegistry.UnsupportedMessageException();
        }
        if (corruptJsonResponses) {
            // Empty json object is a corrupt json object.
            jsonObject = new JSONObject();
        }
        actionInformation.setInformation(jsonObject);
        return actionInformation;
    }

    private JSONObject addContentActionNotExpired(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1000);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addNotificationAction(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject notificationActionObject = new JSONObject();
        notificationActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        notificationActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionNotificationAction.kTypeVariableNotification);
        notificationActionObject.put(RelutionNotificationAction.kContentParameter, "testTitle");
        notificationActionObject.put(RelutionNotificationAction.kIconParameter,
                "http://www.mwaysolutions.com/wp-content/media/2015/12/favicon.ico");
        notificationActionObject.put(RelutionAction.POSTPONE_PARAMETER, RelutionAction.MIN_VALIDITY_BEGINS);
        notificationActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, RelutionAction.MAX_VALIDITY_ENDS);
        actionsArray.put(notificationActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithExpiredValidity(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, -1);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithDelayedValidity(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 1);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1500);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithMissingParameters(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithHighRepeatEveryParameter(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1000);
        // We repeat the message every 5 seconds.
        contentActionObject.put(RelutionAction.REPEAT_EVERY_PARAMETER, 5);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addDelayedNotificationAction(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject notificationActionObject = new JSONObject();
        notificationActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        notificationActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionNotificationAction.kTypeVariableNotification);
        notificationActionObject.put(RelutionNotificationAction.kContentParameter, "testTitle");
        notificationActionObject.put(RelutionNotificationAction.kIconParameter,
                "http://www.mwaysolutions.com/wp-content/media/2015/12/favicon.ico");
        // Action should be triggered 15 seconds later.
        notificationActionObject.put(RelutionAction.POSTPONE_PARAMETER, 15);
        notificationActionObject.put(RelutionAction.VALID_UNTIL_PARAMETER, RelutionAction.MAX_VALIDITY_ENDS);
        actionsArray.put(notificationActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addLockedNotificationAction(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject notificationActionObject = new JSONObject();
        notificationActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        notificationActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionNotificationAction.kTypeVariableNotification);
        notificationActionObject.put(RelutionNotificationAction.kContentParameter, "testTitle");
        notificationActionObject.put(RelutionNotificationAction.kIconParameter,
                "http://www.mwaysolutions.com/wp-content/media/2015/12/favicon.ico");
        notificationActionObject.put(RelutionAction.POSTPONE_PARAMETER, 0);
        notificationActionObject.put(RelutionAction.VALID_UNTIL_PARAMETER, RelutionAction.MAX_VALIDITY_ENDS);
        // Repeat only every 10 seconds the message.
        //notificationActionObject.put(RelutionAction.REPEAT_EVERY_PARAMETER, 10);
        notificationActionObject.put(RelutionAction.REPEAT_EVERY_PARAMETER, 10);
        actionsArray.put(notificationActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithLowRepeatEveryParameter(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, RelutionAction.MAX_VALIDITY_ENDS);
        // We repeat the message every 1 second.
        contentActionObject.put(RelutionAction.REPEAT_EVERY_PARAMETER, 1);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithHighDistanceThreshold(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        // Action should only be executed inside a range of 3 meters.
        contentActionObject.put(RelutionAction.DISTANCE_THRESHOLD_PARAMETER, 3);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithNoSpecifiedDistanceThreshold(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addTagActionWithCheckoutTag(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject visitedActionObject = new JSONObject();
        visitedActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        visitedActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionTagAction.kTypeVariableVisited);
        visitedActionObject.put(RelutionTagAction.kContentParameter, "Checkout");
        actionsArray.put(visitedActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addTagActionWithFoyerTag(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject visitedActionObject = new JSONObject();
        visitedActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        visitedActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionTagAction.kTypeVariableVisited);
        visitedActionObject.put(RelutionTagAction.kContentParameter, "Foyer");
        actionsArray.put(visitedActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithExpiredCampaign(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1000);
        actionsArray.put(contentActionObject);
        addExpiredCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithInactiveCampaign(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "testContent");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1000);
        actionsArray.put(contentActionObject);
        addInactiveCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addTwoContentActionsInDifferentCampaigns(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        // Define campaign 1
        JSONArray actionsArray1 = new JSONArray();
        JSONObject contentActionObject1 = new JSONObject();
        contentActionObject1.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject1.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject1.put(RelutionContentAction.kContentParameter, "testContent");
        actionsArray1.put(contentActionObject1);

        // Define campaign 2
        JSONArray actionsArray2 = new JSONArray();
        JSONObject contentActionObject2 = new JSONObject();
        contentActionObject2.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject2.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject2.put(RelutionContentAction.kContentParameter, "testContent");
        actionsArray2.put(contentActionObject2);

        // Add two campaigns
        JSONObject campaign1 = getDefaultCampaign(actionsArray1);
        JSONObject campaign2 = getDefaultCampaign(actionsArray2);
        JSONArray campaignArray = new JSONArray();
        campaignArray.put(campaign1);
        campaignArray.put(campaign2);
        jsonObject.put(RelutionCampaign.kCampaignsParameter, campaignArray);

        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addTagActionWithFruitsAndVegetablesTag(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();

        JSONObject visitedActionObject1 = new JSONObject();
        visitedActionObject1.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        visitedActionObject1.put(RelutionAction.TYPE_PARAMETER, RelutionTagAction.kTypeVariableVisited);
        visitedActionObject1.put(RelutionTagAction.kContentParameter, "Fruits");

        JSONObject visitedActionObject2 = new JSONObject();
        visitedActionObject2.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        visitedActionObject2.put(RelutionAction.TYPE_PARAMETER, RelutionTagAction.kTypeVariableVisited);
        visitedActionObject2.put(RelutionTagAction.kContentParameter, "Vegetables");

        actionsArray.put(visitedActionObject1);
        actionsArray.put(visitedActionObject2);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithHTMLContent(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter,
                "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                        "Non est ista, inquam, Piso, magna dissensio. " +
                        "Nos paucis ad haec additis finem faciamus aliquando; " +
                        "Quamquam haec quidem praeposita recte et reiecta dicere licebit. " +
                        "Quid enim de amicitia statueris utilitatis causa expetenda vides. " +
                        "Quis est, qui non oderit libidinosam, protervam adolescentiam? " +
                        "At ille pellit, qui permulcet sensum voluptate. <code>Haec igitur " +
                        "Epicuri non probo, inquam.</code> Et nemo nimium beatus est; <b>Duo Reges: " +
                        "constructio interrete.</b> <a href='http://loripsum.net/' target='_blank'>Restatis " +
                        "igitur vos;</a> </p>\n" +
                "\n" +
                "<blockquote cite='http://loripsum.net'>\n" +
                "\tHabebat tamen rationem valitudinis: utebatur iis exercitationibus, " +
                        "ut ad cenam et sitiens et esuriens veniret, eo cibo, qui et " +
                        "suavissimus esset et idem facillimus ad concoquendum, vino et " +
                        "ad voluptatem et ne noceret.\n" +
                "</blockquote>\n" +
                "\n" +
                "\n" +
                "<dl>\n" +
                "\t<dt><dfn>Stoicos roga.</dfn></dt>\n" +
                "\t<dd>Ad corpus diceres pertinere-, sed ea, quae dixi, ad corpusne refers?</dd>\n" +
                "\t<dt><dfn>Sullae consulatum?</dfn></dt>\n" +
                "\t<dd>Claudii libidini, qui tum erat summo ne imperio, dederetur.</dd>\n" +
                "\t<dt><dfn>An eiusdem modi?</dfn></dt>\n" +
                "\t<dd>Nunc omni virtuti vitium contrario nomine opponitur.</dd>\n" +
                "\t<dt><dfn>Quibusnam praeteritis?</dfn></dt>\n" +
                "\t<dd>Si verbum sequimur, primum longius verbum praepositum quam bonum.</dd>\n" +
                "\t<dt><dfn>Tria genera bonorum;</dfn></dt>\n" +
                "\t<dd>Quid enim de amicitia statueris utilitatis causa expetenda vides.</dd>\n" +
                "</dl>\n" +
                "\n" +
                "\n" +
                "<ol>\n" +
                "\t<li>Qui bonum omne in virtute ponit, is potest dicere perfici beatam " +
                        "vitam perfectione virtutis;</li>\n" +
                "\t<li>Sed id ne cogitari quidem potest quale sit, ut non repugnet ipsum sibi.</li>\n" +
                "\t<li>Si longus, levis.</li>\n" +
                "</ol>\n" +
                "\n" +
                "\n" +
                "<ul>\n" +
                "\t<li>An, partus ancillae sitne in fructu habendus, disseretur " +
                        "inter principes civitatis, P.</li>\n" +
                "\t<li>Atque ab his initiis profecti omnium virtutum et originem " +
                        "et progressionem persecuti sunt.</li>\n" +
                "</ul>\n" +
                "\n" +
                "\n" +
                "<pre>\n" +
                "Nihil est enim, de quo aliter tu sentias atque ego, modo\n" +
                "commutatis verbis ipsas res conferamus.\n" +
                "\n" +
                "Non enim hilaritate nec lascivia nec risu aut ioco, comite\n" +
                "levitatis, saepe etiam tristes firmitate et constantia sunt\n" +
                "beati.\n" +
                "</pre>\n" +
                "\n" +
                "\n");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 0);
        contentActionObject.put(RelutionContentAction.REPEAT_EVERY_PARAMETER, 20);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1000);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addNotificationActionWithHighDistanceThreshold(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject notificationActionObject = new JSONObject();
        notificationActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        notificationActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionNotificationAction.kTypeVariableNotification);
        // Action should only be executed inside a range of 10 meters.
        notificationActionObject.put(RelutionAction.DISTANCE_THRESHOLD_PARAMETER, 10);
        notificationActionObject.put(RelutionNotificationAction.kContentParameter, "testContent");
        actionsArray.put(notificationActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithLongDelayedValidity(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "Delayed example");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 10);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1500);
        contentActionObject.put(RelutionContentAction.REPEAT_EVERY_PARAMETER, 20);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    private JSONObject addContentActionWithLockAndDelay(IBeacon iBeacon) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray actionsArray = new JSONArray();
        JSONObject contentActionObject = new JSONObject();
        contentActionObject.put(RelutionAction.ACTION_ID_PARAMETER, UUID.randomUUID().toString());
        contentActionObject.put(RelutionAction.TYPE_PARAMETER, RelutionContentAction.kTypeVariableContent);
        contentActionObject.put(RelutionContentAction.kContentParameter, "Lock-Delay example");
        contentActionObject.put(RelutionContentAction.POSTPONE_PARAMETER, 10);
        contentActionObject.put(RelutionContentAction.VALID_UNTIL_PARAMETER, 1500);
        contentActionObject.put(RelutionContentAction.REPEAT_EVERY_PARAMETER, 20);
        actionsArray.put(contentActionObject);
        addDefaultCampaign(jsonObject, actionsArray);
        iBeaconActionMap.put(iBeacon, jsonObject);
        return jsonObject;
    }

    @Override
    public boolean isAvailable() {
        // Sender is always available
        return true;
    }

    public Map<IBeacon, JSONObject> getIBeaconActionMap() {
        return iBeaconActionMap;
    }

    public void corruptJsons() {
        this.corruptJsonResponses = true;
    }
}
