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
package com.wso2.rfid;

import com.wso2.rfid.apicalls.APICall;
import com.wso2.rfid.apicalls.UserCheckinException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * TODO: class level comment
 */
public class RFIDReaderServlet extends HttpServlet {


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(
                        inputStream));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        String rfid = stringBuilder.toString();
        System.out.println("RFID=" + rfid);
        RaspberryPi me = Main.getMe();
        if ("USER_REGISTRATION".equals(me.getMode())) {
            APICall.userRegistration(me.getMacAddress(), rfid);
        } else {
            RFIDRegistrationBuffer registrationBuffer = RFIDRegistrationBuffer.getInstance();
            if (!registrationBuffer.containsValid(rfid)) {
                try {
                    APICall.userCheckin(me.getMacAddress(), rfid, me.getUserCheckinURL(), me.getConsumerKey(), me.getConsumerSecret());
                    registrationBuffer.add(rfid);
                } catch (UserCheckinException e) {
                    e.printStackTrace();
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
