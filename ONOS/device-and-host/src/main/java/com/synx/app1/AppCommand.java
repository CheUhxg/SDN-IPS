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

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.Device;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostService;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.*;

/**
 * Sample Apache Karaf CLI command
 */


// 创建cli命令
@Service
// scope：命令范围 name：命令名称  description：命令描述
@Command(scope = "onos", name = "getmyinfo",
         description = "Get Device And Host Information")
public class AppCommand extends AbstractShellCommand {
    // 输入命令后执行注解的命令
    private final DeviceAndHostService deviceAndHostService = get(DeviceAndHostService.class);

    @Override
    protected void doExecute(){
        print(deviceAndHostService.getDeviceAndHost().toString());
    }

}
