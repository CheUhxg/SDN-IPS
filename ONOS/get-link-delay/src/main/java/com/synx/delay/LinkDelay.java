/*
 * Copyright 2020-present Open Networking Foundation
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
package com.synx.delay;

import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.packet.*;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.onosproject.net.PortNumber.portNumber;
import static org.onosproject.net.flow.DefaultTrafficTreatment.builder;


/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true,
           service = {LinkDelayService.class}
           )
public class LinkDelay implements LinkDelayService {


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected LinkService linkService;

    //
    private final ScheduledExecutorService sendDelayPacketExecutor = Executors.newScheduledThreadPool(1);
    //
    private DelayPacketProcessor processor = new DelayPacketProcessor();

    private final DeviceListener deviceListener = new InternalDeviceListener();

    private final LinkListener linkListener = new InternalLinkListener();

    private ApplicationId appId;

    Map<String,Long> map = new HashMap<>();

    @Activate
    protected void activate() {
        log.info("Started");
        linkService.addListener(linkListener);
        deviceService.addListener(deviceListener);
        appId = coreService.registerApplication("org.onosproject.core");
        requestIntercepts();
        packetService.addProcessor(processor,PacketProcessor.director(1));
        sendPb();
    }

    @Deactivate
    protected void deactivate() {
        linkService.removeListener(linkListener);
        deviceService.removeListener(deviceListener);
        packetService.removeProcessor(processor);
        processor=null;
        withdrawIntercepts();
        log.info("Stopped");
    }

    @Override
    public Map<String, Long> getLinkDelay() {
        return map;
    }

    /**
     * 数据包处理器。
     */
    private class DelayPacketProcessor implements PacketProcessor{

        @Override
        public void process(PacketContext context) {
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            if(ethPkt.getEtherType()==MyDelay.PROTOCOL_MYDELAY){
                String to=pkt.receivedFrom().deviceId().toString();
                final ByteBuffer bb = ByteBuffer.wrap(ethPkt.getPayload().serialize());

                char[] de = new char[19];
                for(int i=0;i<19;i++){
                    de[i] = bb.getChar();
                }

                String from = String.valueOf(de);
                long interval = (System.nanoTime()) - (bb.getLong());

                map.put(from+" "+to,interval);

                context.block();
            }
        }
    }

    /**
     *
     */
    public void sendPb(){
        sendDelayPacketExecutor.scheduleAtFixedRate(()->{
            Iterable<Device> devices = deviceService.getDevices();

            for(Device d: devices){
                List<Port> portList = deviceService.getPorts(d.id());
                for(Port port:portList){
                    if(!"LOCAL".equals(port.number().toString())){
                        sendProbes(port.number().toLong(),d.id());
                    }
                }
            }
         },3000L,3000L, TimeUnit.MILLISECONDS);
    }

    /**
     *  给指定交换机的端口发数据包
     * @param portNumber packet-out数据包指定的端口号
     * @param deviceId packet-out数据包指定的交换机id
     */
    public void sendProbes(Long portNumber, DeviceId deviceId) {
        if (packetService == null) {
            return;
        }
        log.info("Sending probes out of {}@{}", portNumber, deviceId);
        OutboundPacket pkt = CreateOutBoundMyDelay(portNumber, deviceId);
        if (pkt != null) {
            packetService.emit(pkt);
        } else {
            log.warn("Cannot send mydelay packet due to packet is null {}", deviceId);
        }

    }


    /**
     *  构建数据包
     * @param portNumber 数据包发出的交换机端口
     * @param deviceId  数据包发出的交换机编号
     * @return
     */
    private OutboundPacket CreateOutBoundMyDelay(Long portNumber,DeviceId deviceId){
        if(portNumber == null){
            return null;
        }

        // 构建以太网数据包
        Ethernet ethPacket = genMyDelay(deviceId);

        return new DefaultOutboundPacket(deviceId,
                builder().setOutput(portNumber(portNumber)).build(),
                ByteBuffer.wrap(ethPacket.serialize()));
    }

    /**
     *  构建以太网数据包，包括pdu
     * @param from 源交换机的id
     * @return 构建好的数据包
     */
    public Ethernet genMyDelay(final DeviceId from){
        String  fromdevice = from.toString();

        Ethernet myDelayEth = new Ethernet();
        MyDelay myDelay = new MyDelay(fromdevice,System.nanoTime());

        myDelayEth.setDestinationMACAddress("ff:ff:ff:ff:ff:ff");
        myDelayEth.setSourceMACAddress("ff:ff:ff:ff:ff:ff");
        myDelayEth.setEtherType(MyDelay.PROTOCOL_MYDELAY);
        myDelayEth.setPayload(myDelay);

        return myDelayEth;
    }

    /**
     * 下发流表
     */
    public void requestIntercepts(){
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(MyDelay.PROTOCOL_MYDELAY);
        packetService.requestPackets(selector.build(),
                PacketPriority.CONTROL,appId
        );
    }

    /**
     * 程序结束时删除流表
     */
    public void withdrawIntercepts(){
        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        selector.matchEthType(MyDelay.PROTOCOL_MYDELAY);
        packetService.cancelPackets(selector.build(),
                PacketPriority.CONTROL,appId
        );
    }


    private class InternalLinkListener implements LinkListener {
        @Override
        public void event(LinkEvent event) {
            LinkEvent.Type type =event.type();
            if(type==LinkEvent.Type.LINK_ADDED ||type==LinkEvent.Type.LINK_REMOVED||type==LinkEvent.Type.LINK_UPDATED){
                map.clear();
                log.info("rebuild");
            }
        }
    }

    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            if (type == DeviceEvent.Type.DEVICE_ADDED || type == DeviceEvent.Type.DEVICE_REMOVED) {
                map.clear();
                log.info("rebuild");
            }
        }
    }

}
