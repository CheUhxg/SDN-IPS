package org.onosproject.p4tutorial.mytunnel;

import com.google.common.collect.Lists;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *下发P4定义处理逻辑映射流表
 ×并启动
 */
@Component(immediate = true)
public class MyTunnelApp {

    private static final String APP_NAME = "org.onosproject.p4ddos.ddosflow";

    private static final int FLOW_RULE_PRIORITY = 100;

    private final HostListener hostListener = new InternalHostListener();
    private ApplicationId appId;
    private AtomicInteger nextTunnelId = new AtomicInteger();

    private static final Logger log = getLogger(ddosflow.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private CoreService coreService;


    @Activate
    public void activate() {
        log.info("Starting...");
        appId = coreService.registerApplication(APP_NAME);
        hostService.addListener(hostListener);
        log.info("STARTED", appId.id());
    }

    @Deactivate
    public void deactivate() {
        log.info("Stopping...");
        hostService.removeListener(hostListener);
        flowRuleService.removeFlowRulesById(appId);
        log.info("STOPPED");
    }

    /**
     *
     * 下发转发表
     */
    private void provisionTunnel(int tunId, Host srcHost, Host dstHost, Topology topo) {

        DeviceId srcSwitch = srcHost.location().deviceId();
        DeviceId dstSwitch = dstHost.location().deviceId();

        // 找出最短路径
        List<Link> pathLinks;
        if (srcSwitch.equals(dstSwitch)) {
            pathLinks = Collections.emptyList();
        } else {
            Set<Path> allPaths = topologyService.getPaths(topo, srcSwitch, dstSwitch);
            if (allPaths.size() == 0) {
                log.warn("No paths between {} and {}", srcHost.id(), dstHost.id());
                return;
            }
            pathLinks = pickRandomPath(allPaths).links();
        }

        // Tunnel ingress rules.
        for (IpAddress dstIpAddr : dstHost.ipAddresses()) {
            insertTunnelIngressRule(srcSwitch, dstIpAddr, tunId);
        }

        // 下发转发流表
        for (Link link : pathLinks) {
          // 下发流表的目标交换机+出口端口
            DeviceId sw = link.src().deviceId();
            PortNumber port = link.src().port();
            insertTunnelForwardRule(sw, port, tunId, false);
        }

        // Tunnel egress rule.
        PortNumber egressSwitchPort = dstHost.location().port();
        insertTunnelForwardRule(dstSwitch, egressSwitchPort, tunId, true);

        log.info("** Completed provisioning of tunnel {} (srcHost={} dstHost={})",
                 tunId, srcHost.id(), dstHost.id());
    }

    /**
     * 向网关交换机下发参数调整
     */
    private void insertTunnelIngressRule(DeviceId switchId,
                                         IpAddress dstIpAddr,
                                         int tunId) {


        PiTableId tunnelIngressTableId = PiTableId.of("c_ingress.t_tunnel_ingress");

        PiMatchFieldId ipDestMatchFieldId = PiMatchFieldId.of("hdr.ipv4.dst_addr");
        PiCriterion match = PiCriterion.builder()
                .matchLpm(ipDestMatchFieldId, dstIpAddr.toOctets(), 32)
                .build();

        PiActionParam tunIdParam = new PiActionParam(PiActionParamId.of("tun_id"), tunId);

        PiActionId ingressActionId = PiActionId.of("c_ingress.my_tunnel_ingress");
        PiAction action = PiAction.builder()
                .withId(ingressActionId)
                .withParameter(tunIdParam)
                .build();

        log.info("Inserting INGRESS rule on switch {}: table={}, match={}, action={}",
                 switchId, tunnelIngressTableId, match, action);

        insertPiFlowRule(switchId, tunnelIngressTableId, match, action);
    }

    /**
     * 添加端口映射的转发操作
     */
    private void insertTunnelForwardRule(DeviceId switchId,
                                         PortNumber outPort,
                                         int tunId,
                                         boolean isEgress) {

        PiTableId tunnelForwardTableId = PiTableId.of("c_ingress.t_tunnel_fwd");

        PiMatchFieldId tunIdMatchFieldId = PiMatchFieldId.of("hdr.my_tunnel.tun_id");
        PiCriterion match = PiCriterion.builder()
                .matchExact(tunIdMatchFieldId, tunId)
                .build();

        PiActionParamId portParamId = PiActionParamId.of("port");
        PiActionParam portParam = new PiActionParam(portParamId, (short) outPort.toLong());

        final PiAction action;
        if (isEgress) {
            PiActionId egressActionId = PiActionId.of("c_ingress.my_tunnel_egress");
            action = PiAction.builder()
                    .withId(egressActionId)
                    .withParameter(portParam)
                    .build();
        } else {
            PiActionId egressActionId = PiActionId.of("c_ingress.set_out_port");
            action = PiAction.builder()
                    .withId(egressActionId)
                    .withParameter(portParam)
                    .build();
        }

        log.info("Inserting {} rule on switch {}: table={}, match={}, action={}",
                 isEgress ? "EGRESS" : "TRANSIT",
                 switchId, tunnelForwardTableId, match, action);

        insertPiFlowRule(switchId, tunnelForwardTableId, match, action);
    }



    private int getNewTunnelId() {
        return nextTunnelId.incrementAndGet();
    }

    private Path pickRandomPath(Set<Path> paths) {
        int item = new Random().nextInt(paths.size());
        List<Path> pathList = Lists.newArrayList(paths);
        return pathList.get(item);
    }
}
