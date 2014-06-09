/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package com.wso2.rfid.apicalls;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.misc.BASE64Encoder;

import java.io.IOException;

/**
 * TODO: class level comment
 */
public class APICall {
    private static final Log log = LogFactory.getLog(APICall.class);

    private static String tokenEndpoint = "https://gateway.api.cloud.wso2.com:8243/token";

    public static void setTokenEndpoint(String tokenEndpoint){
        APICall.tokenEndpoint = tokenEndpoint;
    }

    public static void userCheckin(String deviceID, String rfid,
                                   String url,
                                   String consumerKey, String consumerSecret){
//        String url = "https://gateway.apicloud.cloudpreview.wso2.com:8243/t/indikas.com/wso2coniot/1.0.0/conferences/2/userCheckIn";
        Token token = getToken(consumerKey, consumerSecret);
        if (token != null) {
            HttpClient httpClient = new HttpClient();
            JSONObject json = new JSONObject();
            json.put("uuid", deviceID);
            json.put("rfid", rfid);
            try {
                HttpResponse httpResponse =
                        httpClient.doPost(url, "Bearer " + token.getAccessToken(), json.toJSONString(), "application/json");
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if(statusCode != 200){
                    log.error("User checkin failed. HTTP Status Code:" + statusCode + ", RFID: " + rfid);
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }

    private static Token getToken(String consumerKey, String consumerSecret) {
        HttpClient httpClient = new HttpClient();
//        String consumerKey = PizzaShackWebConfiguration.getInstance().getConsumerKey();
//        String consumerSecret = PizzaShackWebConfiguration.getInstance().getConsumerSecret();
        try {
            String applicationToken = consumerKey + ":" + consumerSecret;
            BASE64Encoder base64Encoder = new BASE64Encoder();
            applicationToken = "Basic " + base64Encoder.encode(applicationToken.getBytes()).trim();

//            String payload = "grant_type=password&username="+username+"&password="+password;
            String payload = "grant_type=client_credentials";
            HttpResponse httpResponse = httpClient.doPost(tokenEndpoint, applicationToken,
                    payload, "application/x-www-form-urlencoded");
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            String response = httpClient.getResponsePayload(httpResponse);
            return getAccessToken(response);
        } catch (IOException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * Populates Token object using folloing JSON String
     * {
     * "token_type": "bearer",
     * "expires_in": 3600000,
     * "refresh_token": "f43de118a489d56c3b3b7ba77a1549e",
     * "access_token": "269becaec9b8b292906b3f9e69b5a9"
     * }
     *
     * @param accessTokenJson
     * @return
     */
    public static Token getAccessToken(String accessTokenJson) {
        JSONParser parser = new JSONParser();
        Token token = new Token();
        try {
            Object obj = parser.parse(accessTokenJson);
            JSONObject jsonObject = (JSONObject) obj;
            token.setAccessToken((String) jsonObject.get("access_token"));
            long expiresIn = ((Long) jsonObject.get("expires_in")).intValue();
            token.setExpiresIn(expiresIn);
            token.setRefreshToken((String) jsonObject.get("refresh_token"));
            token.setTokenType((String) jsonObject.get("token_type"));

        } catch (ParseException e) {
            log.error("", e);
        }
        return token;
    }
}
