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
package com.synx.app1;

import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.Device;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.onlab.util.Tools.get;

/**
 * Skeletal ONOS application component.
 */



@Component(immediate = true,service = {DeviceAndHostService.class})
public class AppComponent  implements DeviceAndHostService{

    // 引入网络设备相关的服务 cardinality可以描述引用基数
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;
   // 引入主机相关的服务
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;


    private final Logger log = LoggerFactory.getLogger(getClass());
    // 添加两个监听器，监听器必须实现event method
    private final DeviceListener deviceListener = new InternalDeviceListener();
    private final HostListener hostListener = new InternalHostListener();


    // osgi注解，是这一应用的入口，应用载入过后从这里开始运行。
    @Activate
    protected void activate() {
        log.info("Started");
        deviceService.addListener(deviceListener);
        hostService.addListener(hostListener);

    }


    // 应用的出口，应用被停止后将执行被它所注解的函数，通常进行一些清理工作
    @Deactivate
    protected void deactivate() {
        deviceService.removeListener(deviceListener);
        hostService.removeListener(hostListener);
        log.info("Stopped");
    }


    private class InternalDeviceListener implements DeviceListener{
        @Override
        public void event(DeviceEvent event){
            DeviceEvent.Type type = event.type();
            if(type == DeviceEvent.Type.DEVICE_ADDED || type==DeviceEvent.Type.DEVICE_REMOVED){
                log.info(getDeviceAndHost().toString());
            }
        }
    }

    @Override
    public Map<String,List<String>> getDeviceAndHost() {
        Map<String,List<String>> map = new HashMap<>();
        Iterable<org.onosproject.net.Device> devices = deviceService.getDevices();
        for(Device device: devices){
            Set<Host> hosts = hostService.getConnectedHosts(device.id());
            List<String> hostList = new ArrayList<>();
            for(Host host:hosts){
                hostList.add(host.ipAddresses().toString());
            }
            map.put(device.id().toString(),hostList);
        }
        return map;
    }


    private class InternalHostListener implements HostListener{
        @Override
        public void event(HostEvent event){
            getDeviceAndHost().toString();
        }
    }
}
