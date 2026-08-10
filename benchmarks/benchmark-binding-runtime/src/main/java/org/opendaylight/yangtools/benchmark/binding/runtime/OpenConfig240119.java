/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.benchmark.binding.runtime;

import java.util.List;
import org.opendaylight.yang.gen.v1.http.example.net.yang.openconfig.terminal.device.properties.rev220426.OpenconfigTerminalDevicePropertiesData;
import org.opendaylight.yang.gen.v1.http.example.net.yang.openconfig.terminal.device.property.types.rev220308.OpenconfigTerminalDevicePropertyTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.aaa.rev200730.OpenconfigAaaData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.aaa.types.rev181121.OpenconfigAaaTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.acl.rev210616.OpenconfigAclData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.aft.rev220517.OpenconfigAftData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.alarms.rev190709.OpenconfigAlarmsData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.alarms.types.rev181121.OpenconfigAlarmTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bfd.rev210616.OpenconfigBfdData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev220521.OpenconfigBgpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev210806.OpenconfigBgpTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.evpn.rev210628.OpenconfigEvpnData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.evpn.types.rev210621.OpenconfigEvpnTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.fib.types.rev210824.OpenconfigAftTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.header.fields.rev210616.OpenconfigPacketMatchData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.igmp.rev210517.OpenconfigIgmpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.igmp.types.rev181121.OpenconfigIgmpTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.aggregate.rev200501.OpenconfigIfAggregateData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ethernet.rev220420.OpenconfigIfEthernetData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev210406.OpenconfigInterfacesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.isis.lsdb.types.rev181121.OpenconfigIsisLsdbTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.isis.types.rev220211.OpenconfigIsisTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ldp.rev200109.OpenconfigMplsLdpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.license.rev200422.OpenconfigLicenseData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev181121.OpenconfigLldpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.types.rev181121.OpenconfigLldpTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev220510.OpenconfigLocalRoutingData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.messages.rev180813.OpenconfigMessagesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.rev210728.OpenconfigMplsData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.sr.rev181121.OpenconfigMplsSrData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.mpls.types.rev211201.OpenconfigMplsTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.l3.rev181121.OpenconfigNetworkInstanceL3Data;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev210714.OpenconfigNetworkInstanceTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.oc.keychain.rev220305.OpenconfigKeychainData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.oc.keychain.types.rev220301.OpenconfigKeychainTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig._if.types.rev181121.OpenconfigIfTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.ext.rev200616.OpenconfigExtensionsData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.isis.rev220510.OpenconfigIsisData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.transport.line.connectivity.rev190627.OpenconfigTransportLineConnectivityData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.types.rev190416.OpenconfigTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openflow.types.rev220524.OpenconfigOpenflowTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospf.types.rev181121.OpenconfigOspfTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev220210.OpenconfigOspfv2Data;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.packet.match.types.rev210714.OpenconfigPacketMatchTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.pcep.rev220211.OpenconfigPcepData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.pim.rev210616.OpenconfigPimData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.pim.types.rev181121.OpenconfigPimTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.cpu.rev181121.OpenconfigPlatformCpuData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.extension.rev181121.OpenconfigPlatformExtData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.fan.rev181121.OpenconfigPlatformFanData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.linecard.rev220421.OpenconfigPlatformLinecardData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.port.rev211001.OpenconfigPlatformPortData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.psu.rev181121.OpenconfigPlatformPsuData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.rev220610.OpenconfigPlatformData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.transceiver.rev210729.OpenconfigPlatformTransceiverData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.platform.types.rev220327.OpenconfigPlatformTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.policy.forwarding.rev220125.OpenconfigPolicyForwardingData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.policy.types.rev211210.OpenconfigPolicyTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev220606.OpenconfigRibBgpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.types.rev190314.OpenconfigRibBgpTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.routing.policy.rev220524.OpenconfigRoutingPolicyData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev220327.OpenconfigMplsRsvpData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.segment.routing.srte.policy.rev210728.OpenconfigSrtePolicyData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.segment.routing.types.rev200204.OpenconfigSegmentRoutingTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.sr.rev210728.OpenconfigSegmentRoutingData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.system.logging.rev181121.OpenconfigSystemLoggingData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.system.procmon.rev190315.OpenconfigProcmonData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.system.rev210720.OpenconfigSystemData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.system.terminal.rev181121.OpenconfigSystemTerminalData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev181121.OpenconfigTelemetryData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.types.rev181121.OpenconfigTelemetryTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.terminal.device.rev210729.OpenconfigTerminalDeviceData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.transport.line.common.rev190603.OpenconfigTransportLineCommonData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.transport.types.rev210729.OpenconfigTransportTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev210817.OpenconfigInetTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.yang.rev210714.OpenconfigYangTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.rev210728.OpenconfigVlanData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.types.rev220524.OpenconfigVlanTypesData;

final class OpenConfig240119 {
    private OpenConfig240119() {
        // hidden on purpose
    }

    static List<Class<?>> classes() {
        return List.of(
            OpenconfigTerminalDevicePropertiesData.class,
            OpenconfigTerminalDevicePropertyTypesData.class,
            OpenconfigAaaData.class,
            OpenconfigAaaTypesData.class,
            OpenconfigAclData.class,
            OpenconfigAftData.class,
            OpenconfigAlarmsData.class,
            OpenconfigAlarmTypesData.class,
            OpenconfigBfdData.class,
            OpenconfigBgpData.class,
            OpenconfigBgpTypesData.class,
            OpenconfigEvpnData.class,
            OpenconfigEvpnTypesData.class,
            OpenconfigAftTypesData.class,
            OpenconfigPacketMatchData.class,
            OpenconfigIgmpData.class,
            OpenconfigIgmpTypesData.class,
            OpenconfigIfAggregateData.class,
            OpenconfigIfEthernetData.class,
            OpenconfigInterfacesData.class,
            OpenconfigIsisLsdbTypesData.class,
            OpenconfigIsisTypesData.class,
            OpenconfigMplsLdpData.class,
            OpenconfigLicenseData.class,
            OpenconfigLldpData.class,
            OpenconfigLldpTypesData.class,
            OpenconfigLocalRoutingData.class,
            OpenconfigMessagesData.class,
            OpenconfigMplsData.class,
            OpenconfigMplsSrData.class,
            OpenconfigMplsTypesData.class,
            OpenconfigNetworkInstanceL3Data.class,
            OpenconfigNetworkInstanceTypesData.class,
            OpenconfigKeychainData.class,
            OpenconfigKeychainTypesData.class,
            OpenconfigExtensionsData.class,
            OpenconfigIfTypesData.class,
            OpenconfigIsisData.class,
            OpenconfigTransportLineConnectivityData.class,
            OpenconfigTypesData.class,
            OpenconfigOpenflowTypesData.class,
            OpenconfigOspfTypesData.class,
            OpenconfigOspfv2Data.class,
            OpenconfigPacketMatchTypesData.class,
            OpenconfigPcepData.class,
            OpenconfigPimData.class,
            OpenconfigPimTypesData.class,
            OpenconfigPlatformCpuData.class,
            OpenconfigPlatformExtData.class,
            OpenconfigPlatformFanData.class,
            OpenconfigPlatformLinecardData.class,
            OpenconfigPlatformPortData.class,
            OpenconfigPlatformPsuData.class,
            OpenconfigPlatformData.class,
            OpenconfigPlatformTransceiverData.class,
            OpenconfigPlatformTypesData.class,
            OpenconfigPolicyForwardingData.class,
            OpenconfigPolicyTypesData.class,
            OpenconfigRibBgpData.class,
            OpenconfigRibBgpTypesData.class,
            OpenconfigRoutingPolicyData.class,
            OpenconfigMplsRsvpData.class,
            OpenconfigSrtePolicyData.class,
            OpenconfigSegmentRoutingTypesData.class,
            OpenconfigSegmentRoutingData.class,
            OpenconfigSystemLoggingData.class,
            OpenconfigProcmonData.class,
            OpenconfigSystemData.class,
            OpenconfigSystemTerminalData.class,
            OpenconfigTelemetryData.class,
            OpenconfigTelemetryTypesData.class,
            OpenconfigTerminalDeviceData.class,
            OpenconfigTransportLineCommonData.class,
            OpenconfigTransportTypesData.class,
            OpenconfigInetTypesData.class,
            OpenconfigYangTypesData.class,
            OpenconfigVlanData.class,
            OpenconfigVlanTypesData.class);
    }
}
