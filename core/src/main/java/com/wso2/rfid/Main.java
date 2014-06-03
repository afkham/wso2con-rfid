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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TODO: class level comment
 */
public class Main {
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) {
        String rpiControlCenterIP = System.getProperty("rpiControlCenterIP", "10.100.1.192");

        // Read device ID
        String deviceID = readDeviceID();

        Server server = new Server(InetSocketAddress.createUnresolved("127.0.0.1", 8084));
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(RFIDReaderServlet.class, "/rfid");
        scheduler.scheduleWithFixedDelay(new MonitoringTask(rpiControlCenterIP), 0, 10, TimeUnit.SECONDS);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readDeviceID() {
        //TODO: contact RPi Control Center & get Device ID & Device Label
        // Get all teh relevant info such as label, location, device ID etc. from control center
        return null;
    }

    private static class MonitoringTask implements Runnable {
        private String rpiControlCenterIP;

        public MonitoringTask(String rpiControlCenterIP) {

            this.rpiControlCenterIP = rpiControlCenterIP;
        }

        @Override
        public void run() {
            try {
                NetworkAddress networkAddress = new NetworkAddress();
                if (networkAddress.getMacAddress() != null) {
                    String controlCenterUrl = "http://" + rpiControlCenterIP + ":9763/rpi/addme.jsp?mymac=" +
                            networkAddress.getMacAddress() + "&myip=" + networkAddress.getIpV4Address();
                    HttpClient client = HttpClientBuilder.create().build();
                    HttpGet get = new HttpGet(controlCenterUrl);
                    client.execute(get);

                    //As the response, get back information about the device (Label, location, blink, reboot etc.)
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
