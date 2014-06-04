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

import com.wso2.rfid.apicalls.HttpClient;
import org.apache.http.HttpResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    private static RaspberryPi me;

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
        private HttpClient httpClient = new HttpClient();

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
                    HttpResponse httpResponse = httpClient.doGet(controlCenterUrl, null);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        JSONParser parser = new JSONParser();
                        JSONObject obj = (JSONObject) parser.parse(httpClient.getResponsePayload(httpResponse));
                        me = getRaspberryPi(obj);

                    } else {
                        System.err.println("Could not register Raspberry Pi. HTTP Status Code: " + statusCode);
                    }

                    //As the response, get back information about the device (Label, location, blink, reboot etc.)
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        private RaspberryPi getRaspberryPi(JSONObject obj) {
            RaspberryPi rpi = new RaspberryPi();
            rpi.setMacAddress((String) obj.get("mac"));
            rpi.setIpAddress((String) obj.get("ip"));
            rpi.setZoneID((String) obj.get("zoneID"));
            rpi.setConsumerKey((String) obj.get("ck"));
            rpi.setConsumerSecret((String) obj.get("cs"));
            rpi.setBlink((Boolean) obj.get("blink"));
            rpi.setReboot((Boolean) obj.get("reboot"));
            return rpi;
        }
    }

    public static RaspberryPi getMe() {
        return me;
    }
}
