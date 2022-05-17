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
package com.test.defense;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.IpPrefix;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.flow.*;
import org.onosproject.rest.AbstractWebResource;
import org.onosproject.net.Device;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.onlab.util.Tools.nullIsIllegal;
import static org.onlab.util.Tools.readTreeFromStream;

/**
 * Apply the defense strategy sent by backend.
 */
@Path("defense")
public class AppWebResource extends AbstractWebResource {

    public static final short ETH_TYPE = 0x800;
    private static final int APPID = 666;
    private static final int PRIORITY = 60000;
    private static final String IP = "ip";
    private static final String TIMEOUT = "timeout";
    private static final int DEFAULT_TIMEOUT = 60;

    private static final String FLOW_ARRAY_REQUIRED = "Flows array was not specified";
    private static final String FLOW_INT_REQUIRED = "Flows int was not specified";
    private final Logger log = LoggerFactory.getLogger(getClass());

    //引入 Device 服务, 在实验7中，下面这样写就行，但是我在这里面写，就空指针异常。
    //@Reference(cardinality = ReferenceCardinality.MANDATORY)
    //protected DeviceService deviceService;
    protected DeviceService deviceService = get(DeviceService.class);

    //这种引入方式和上面的有区别？
    FlowRuleService flowRuleService = get(FlowRuleService.class);

    /**
     * Defense-apply
     *
     * @return 200 OK
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    public Response getGreeting(InputStream stream) {

        log.info("!!!!!!!!!"+deviceService.toString());
        /**
         * 将 JSON流 读取为 树模型
         */
        ObjectNode jsonTree = null;
        try {//去看看 Onos官方的POST接口是如何处理的
            jsonTree = readTreeFromStream(mapper(), stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("!!!!!!!!!"+jsonTree.toString());

        /**
         * 如果(ArrayNode) jsonTree.get(FLOWS)为null,报错错误异常,报错信息 字符串参数为 FLOW_ARRAY_REQUIRED
         */
        ArrayNode flowsArray = nullIsIllegal((ArrayNode) jsonTree.get(IP),
                FLOW_ARRAY_REQUIRED);
        /** The reason to use:   public int asInt(int defaultValue)
         *      If representation cannot be converted to an int (including structured types like Objects and Arrays),
         *      specified defaultValue will be returned; no exceptions are thrown.
         */
        int timeout = nullIsIllegal(jsonTree.get(TIMEOUT).asInt(DEFAULT_TIMEOUT),  //这里我设置了个 defaultValue
                FLOW_INT_REQUIRED);


/*        String[] IPs = new String[flowsArray.size()]; //好像不需要用数组存
        for (int i = 0; i < number.length; i++) {
            Arrays.fill(number, i);
            System.out.println("number[" + i + "]=" + i);
        }*/
        log.info("!!!!!!!!!"+flowsArray.toString());

        //从 rootNode 中获取数组节点
        for(int i = 0;i < flowsArray.size();i++) {
            String ipToBan = flowsArray.get(i).asText();
            //Todo: 获取系统中所有交换机的集合。

            Iterable<Device> devices = deviceService.getDevices();

            //Todo：遍历交换机，得到它的id，放入某个流表实体中
            for (Device device : devices) {//遍历交换机

                /**Todo: 新建流表实体类
                 *  1. 如果只建一个实体类, 然后每次更改实体类的 DeviceID 后下发该流表, 是否可行?
                 *  2. 每次循环, 新建一个实体类
                 */
                //FlowRule rule = FlowRule.Builder.build(); //用这个行不行呢?
                FlowRule.Builder fb = DefaultFlowRule.builder();
                /* General flow information */
                //fb.forDevice(DeviceId.deviceId("of:00000000000000b0"));//这里放交换机id,不如device.id().toString()
                fb.forDevice(device.id());
                //fb.makeTemporary(timeout);   //经过之前的测试, 这个默认是软超时诶
                //fb.withIdleTimeout(timeout);
                fb.withHardTimeout(timeout);
                fb.withPriority(PRIORITY);
                fb.fromApp(new TestApplicationId(666, "666"));
                log.info("!!!!!!!!!第(*"+i+"*)个ip的流表实体构建完毕！");

                /** Todo: 新建选择器  LinkDelay 102
                 */
                TrafficSelector.Builder sb = DefaultTrafficSelector.builder();
                sb.matchIPSrc(IpPrefix.valueOf(ipToBan + "/32")).matchEthType(ETH_TYPE);
                fb.withSelector(sb.build());
                log.info("!!!!!!!!!第(*"+i+"*)个ip的流表选择器构建完毕！");

                /* Flow treatment */
                //TrafficTreatment.Builder tb = DefaultTrafficTreatment.emptyTreatment();
                fb.withTreatment(DefaultTrafficTreatment.emptyTreatment());
                log.info("!!!!!!!!!第(*"+i+"*)个ip的流表处理器构建完毕！");
                /* Flow applying */
                FlowRule f = fb.build();
                this.flowRuleService.applyFlowRules(f);
                log.info("!!!!!!!!!第(*"+i+"*)个ip的下发给交换机:"+device.id().toString());

            }
            log.info("!!!!!!!!!第(*"+i+"*)个ip的防御策略下发完毕！");

        }
        log.info("!!!!!!!!!所有防御策略下发完毕！");
        ObjectNode node = mapper().createObjectNode().put("Status", "Done!");
        return ok(node).build();


    }
}

class TestApplicationId extends DefaultApplicationId {
    public TestApplicationId(int id, String name) {
        super(id, name);
    }
}
