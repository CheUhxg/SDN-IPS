/*
 * Copyright 2021-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.test.packetcounter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.onosproject.net.Device;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortStatistics;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("pktcounter")
public class AppWebResource extends AbstractWebResource {

    private final DeviceService deviceService = get(DeviceService.class);
    //JsonNodeFactory 实例，可全局共享
    private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    /**
     * 返回每个交换机每个端口所收到的数据包的数量
     *
     *数据格式:
     * {
     *             "deviceID" : pktNumber,
     *             "deviceID" : pktNumber,
     *             "deviceID" : pktNumber
     * }
     *
     * @return 200 OK
     */
    @GET
    @Path("")
    public Response getPktCounter() {

        ObjectNode node = mapper().createObjectNode();

        //ArrayNode counter = node.putArray("counter"); //数据格式_old

        Iterable<Device> devices = deviceService.getDevices();


        for (Device device : devices) {//遍历交换机

            //ObjectNode deviceNode = jsonNodeFactory.objectNode();

            List<PortStatistics> portStatisticsList = deviceService.getPortStatistics(device.id());

            //遍历该交换机各个端口所接收到的流量包数量，并相加
            long sum = 0;
            for (PortStatistics portStatistics : portStatisticsList) {
                sum += portStatistics.packetsReceived();
            }
            //deviceNode.put(device.id().toString(), sum);//数据格式_old
            //counter.add(deviceNode);//数据格式_old
            node.put(device.id().toString(),sum);
        }

        return ok(node).build();

        /**   这是之前准备从 PortStatisticsDiscoveryImly 那个类获取数据时准备写的代码，但是其实没有必要那么麻烦
         *
         *          * //这样强转是否可以？https://blog.csdn.net/lovingning/article/details/79990856
         *          * Collection<PortStatistics> portStatisticsTemp = new PortStatisticsDiscoveryImpl().discoverPortStatistics();
         *          * List presenter =portStatisticsTemp;
         *          * List<DefaultPortStatistics> portStatistics=presenter;
         *
         *         //Iterator<PortStatistics> iter = portStatisticsTemp.iterator();
         *for (Iterator<PortStatistics> iter = portStatisticsTemp.iterator(); iter.hasNext(); ) {
         *PortStatistics portStatistics = (PortStatistics) iter.next();
         *
         *}
         */

        /**
         * 数据格式_old：
         * {
         *     "counter":[
         *         {
         *             "deviceID" : pktNumber
         *         },{
         *             "deviceID" : pktNumber
         *         },{
         *             "deviceID" : pktNumber
         *         }
         *     ]
         * }
         */
    }

}
