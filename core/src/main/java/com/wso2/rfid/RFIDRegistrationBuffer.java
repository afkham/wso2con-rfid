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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: class level comment
 */
public class RFIDRegistrationBuffer {
    private static final long REGISTRATION_TIMEOUT = 30 * 60 * 1000;

    private static RFIDRegistrationBuffer instance = new RFIDRegistrationBuffer();
    private Map<String, Long> rfidMap = new HashMap<>();

    private RFIDRegistrationBuffer() {

    }

    public static RFIDRegistrationBuffer getInstance() {
        return instance;
    }

    public boolean containsValid(String rfid) {
        long now = System.currentTimeMillis();
        return rfidMap.containsKey(rfid) && now - rfidMap.get(rfid) < REGISTRATION_TIMEOUT;
    }

    public void add(String rfid) {
        long now = System.currentTimeMillis();
        rfidMap.put(rfid, now);
        System.out.println("Registered RFID: " + rfid + " at " + new Date(now));
    }
}
