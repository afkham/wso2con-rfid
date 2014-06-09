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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * TODO: class level comment
 */
public class NetworkAddress {
    private String macAddress;
    private String ipV4Address;

    public NetworkAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            System.out.println("networkInterface = " + networkInterface.getDisplayName());
//            if (networkInterface.getDisplayName().startsWith("en0")) { //TODO: change
            if (networkInterface.getDisplayName().startsWith("eth0")) { //TODO: change
//            if (networkInterface.getDisplayName().startsWith("wlan")) {
                byte[] macAddrs = networkInterface.getHardwareAddress();
                Enumeration ee = networkInterface.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (!i.getHostAddress().contains(":")) {
                        this.ipV4Address = i.getHostAddress();
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < macAddrs.length; i++) {
                    sb.append(String.format("%02X%s", macAddrs[i], (i < macAddrs.length - 1) ? "-" : ""));
                }
                this.macAddress = sb.toString();
                System.out.println("macAddress = " + macAddress);
            }
        }
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getIpV4Address() {
        return ipV4Address;
    }
}
