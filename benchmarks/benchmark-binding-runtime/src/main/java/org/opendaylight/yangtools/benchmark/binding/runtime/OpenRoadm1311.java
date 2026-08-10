/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.benchmark.binding.runtime;

import java.util.List;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.openconfig.ext.rev170411.OpenconfigExtensionsData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.rev170824.OpenconfigTelemetryData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.telemetry.types.rev170824.OpenconfigTelemetryTypesData;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170824.OpenconfigInetTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev250110.OrgOpenroadmAlarmData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.amplifier.rev210924.OrgOpenroadmAmplifierData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ber.test.rev250110.OrgOpenroadmBerTestData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.clli.network.rev191129.OrgOpenroadmClliNetworkData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.alarm.pm.types.rev191129.OrgOpenroadmCommonAlarmPmTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.amplifier.types.rev191129.OrgOpenroadmCommonAmplifierTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.attributes.rev250110.OrgOpenroadmCommonAttributesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.ber.test.rev200529.OrgOpenroadmCommonBerTestData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.equipment.types.rev191129.OrgOpenroadmCommonEquipmentTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.link.types.rev191129.OrgOpenroadmCommonLinkTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.network.rev250110.OrgOpenroadmCommonNetworkData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.node.types.rev210528.OrgOpenroadmCommonNodeTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.optical.channel.types.rev250110.OrgOpenroadmCommonOpticalChannelTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.phy.codes.rev250110.OrgOpenroadmCommonPhyCodesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.service.types.rev250110.OrgOpenroadmCommonServiceTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.state.types.rev191129.OrgOpenroadmCommonStateTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.types.rev250110.OrgOpenroadmCommonTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.controller.customization.rev250110.OrgOpenroadmControllerCustomizationData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.database.rev250110.OrgOpenroadmDatabaseData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.de.operations.rev250110.OrgOpenroadmDeOperationsData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.de.swdl.rev250110.OrgOpenroadmSwdlData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.degree.rev250110.OrgOpenroadmDegreeData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev250110.OrgOpenroadmDeviceData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.types.rev221209.OrgOpenroadmDeviceTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.dhcp.rev250110.OrgOpenroadmDhcpData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.equipment.states.types.rev191129.OrgOpenroadmEquipmentStatesTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ethernet.interfaces.rev250110.OrgOpenroadmEthernetInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.external.pluggable.rev250110.OrgOpenroadmExternalPluggableData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.fcc.interfaces.rev250110.OrgOpenroadmFccInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.file.transfer.rev250110.OrgOpenroadmFileTransferData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.fwdl.rev250110.OrgOpenroadmFwdlData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.gcc.interfaces.rev250110.OrgOpenroadmGccInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.gnmi.rev250110.OrgOpenroadmGnmiData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.interfaces.rev220930.OrgOpenroadmInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ip.rev250110.OrgOpenroadmIpData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ipv4.unicast.routing.rev250110.OrgOpenroadmIpv4UnicastRoutingData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ipv6.unicast.routing.rev250110.OrgOpenroadmIpv6UnicastRoutingData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.key.chain.rev191129.OrgOpenroadmKeyChainData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.layerrate.rev191129.OrgOpenroadmLayerRateData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.link.rev250110.OrgOpenroadmLinkData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.lldp.rev250110.OrgOpenroadmLldpData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ltp.template.rev230526.OrgOpenroadmLtpTemplateData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.maintenance.loopback.rev200925.OrgOpenroadmMaintenanceLoopbackData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.maintenance.testsignal.rev250110.OrgOpenroadmMaintenanceTestsignalData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.manifest.file.rev250110.OrgOpenroadmManifestFileData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.media.channel.interfaces.rev250110.OrgOpenroadmMediaChannelInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.network.media.channel.interfaces.rev250110.OrgOpenroadmNetworkMediaChannelInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.network.resource.rev191129.OrgOpenroadmNetworkResourceData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.network.rev250110.OrgOpenroadmNetworkData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.network.topology.rev250110.OrgOpenroadmNetworkTopologyData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.network.topology.types.rev250110.OrgOpenroadmNetworkTopologyTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.network.types.rev250110.OrgOpenroadmNetworkTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.operational.mode.catalog.rev250110.OrgOpenroadmOperationalModeCatalogData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.optical.channel.interfaces.rev250110.OrgOpenroadmOpticalChannelInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.optical.channel.tributary.signal.interfaces.rev250110.OrgOpenroadmOpticalTributarySignalInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.optical.operational.interfaces.rev250110.OrgOpenroadmOpticalOperationalInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.optical.transport.interfaces.rev250110.OrgOpenroadmOpticalTransportInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.ospf.rev250110.OrgOpenroadmOspfData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.otn.common.rev250110.OrgOpenroadmOtnCommonData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.otn.common.types.rev250110.OrgOpenroadmOtnCommonTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.otn.network.topology.rev250110.OrgOpenroadmOtnNetworkTopologyData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.otn.odu.interfaces.rev250110.OrgOpenroadmOtnOduInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.otn.otu.interfaces.rev250110.OrgOpenroadmOtnOtuInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.otsi.group.interfaces.rev250110.OrgOpenroadmOtsiGroupInterfacesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.physical.types.rev191129.OrgOpenroadmPhysicalTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pluggable.optics.holder.capability.rev250110.OrgOpenroadmPluggableOpticsHolderCapabilityData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev250110.OrgOpenroadmPmData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev221209.OrgOpenroadmPmTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.port.capability.rev250110.OrgOpenroadmPortCapabilityData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.port.types.rev250110.OrgOpenroadmPortTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.probablecause.rev230331.OrgOpenroadmProbableCauseData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.prot.equipment.aps.rev250110.OrgOpenroadmProtEquipmentApsData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.prot.otn.linear.aps.rev250110.OrgOpenroadmProtOtnLinearApsData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev250110.OrgOpenroadmResourceData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.types.rev250110.OrgOpenroadmResourceTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.roadm.rev191129.OrgOpenroadmRoadmData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.routing.constraints.rev221209.OrgOpenroadmRoutingConstraintsData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.routing.rev250110.OrgOpenroadmRoutingData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.rstp.rev250110.OrgOpenroadmRstpData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.security.rev250110.OrgOpenroadmSecurityData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.service.format.rev191129.OrgOpenroadmServiceFormatData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.service.rev250110.OrgOpenroadmServiceData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.srg.rev250110.OrgOpenroadmSrgData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.switching.pool.types.rev191129.OrgOpenroadmSwitchingPoolTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.syslog.rev191129.OrgOpenroadmSyslogData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.tca.rev250110.OrgOpenroadmTcaData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.telemetry.types.rev191129.OrgOpenroadmTelemetryTypesData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.topology.rev250110.OrgOpenroadmTopologyData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.user.mgmt.rev200925.OrgOpenroadmUserMgmtData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.wavelength.map.rev191129.OrgOpenroadmWavelengthMapData;
import org.opendaylight.yang.gen.v1.http.org.openroadm.xponder.rev250110.OrgOpenroadmXponderData;
import org.opendaylight.yangtools.binding.meta.RootMeta;

final class OpenRoadm1311 {
    private OpenRoadm1311() {
        // hidden on purpose
    }

    static List<RootMeta<?>> classes() {
        return List.of(
                // common-13.1.1
            OrgOpenroadmAlarmData.META,
            OrgOpenroadmCommonAlarmPmTypesData.META,
            OrgOpenroadmCommonAmplifierTypesData.META,
            OrgOpenroadmCommonAttributesData.META,
            OrgOpenroadmCommonEquipmentTypesData.META,
            OrgOpenroadmCommonLinkTypesData.META,
            OrgOpenroadmCommonNodeTypesData.META,
            OrgOpenroadmCommonOpticalChannelTypesData.META,
            OrgOpenroadmCommonPhyCodesData.META,
            OrgOpenroadmCommonStateTypesData.META,
            OrgOpenroadmCommonTypesData.META,
            OrgOpenroadmEquipmentStatesTypesData.META,
            OrgOpenroadmInterfacesData.META,
            OrgOpenroadmLayerRateData.META,
            OrgOpenroadmLtpTemplateData.META,
            OrgOpenroadmManifestFileData.META,
            OrgOpenroadmNetworkResourceData.META,
            OrgOpenroadmOtnCommonTypesData.META,
            OrgOpenroadmPmData.META,
            OrgOpenroadmPmTypesData.META,
            OrgOpenroadmPortTypesData.META,
            OrgOpenroadmProbableCauseData.META,
            OrgOpenroadmResourceData.META,
            OrgOpenroadmResourceTypesData.META,
            OrgOpenroadmServiceFormatData.META,
            OrgOpenroadmSwitchingPoolTypesData.META,
            OrgOpenroadmTcaData.META,
            OrgOpenroadmUserMgmtData.META,

            // device-13.1.1
            OpenconfigExtensionsData.META,
            OpenconfigTelemetryData.META,
            OpenconfigTelemetryTypesData.META,
            OpenconfigInetTypesData.META,
            OrgOpenroadmDatabaseData.META,
            OrgOpenroadmDeOperationsData.META,
            OrgOpenroadmSwdlData.META,
            OrgOpenroadmDeviceData.META,
            OrgOpenroadmDeviceTypesData.META,
            OrgOpenroadmDhcpData.META,
            OrgOpenroadmEthernetInterfacesData.META,
            OrgOpenroadmFccInterfacesData.META,
            OrgOpenroadmFileTransferData.META,
            OrgOpenroadmFwdlData.META,
            OrgOpenroadmGccInterfacesData.META,
            OrgOpenroadmGnmiData.META,
            OrgOpenroadmIpData.META,
            OrgOpenroadmIpv4UnicastRoutingData.META,
            OrgOpenroadmIpv6UnicastRoutingData.META,
            OrgOpenroadmKeyChainData.META,
            OrgOpenroadmLldpData.META,
            OrgOpenroadmMaintenanceLoopbackData.META,
            OrgOpenroadmMaintenanceTestsignalData.META,
            OrgOpenroadmMediaChannelInterfacesData.META,
            OrgOpenroadmNetworkMediaChannelInterfacesData.META,
            OrgOpenroadmOpticalChannelInterfacesData.META,
            OrgOpenroadmOpticalTributarySignalInterfacesData.META,
            OrgOpenroadmOpticalOperationalInterfacesData.META,
            OrgOpenroadmOpticalTransportInterfacesData.META,
            OrgOpenroadmOspfData.META,
            OrgOpenroadmOtnCommonData.META,
            OrgOpenroadmOtnOduInterfacesData.META,
            OrgOpenroadmOtnOtuInterfacesData.META,
            OrgOpenroadmOtsiGroupInterfacesData.META,
            OrgOpenroadmPhysicalTypesData.META,
            OrgOpenroadmPluggableOpticsHolderCapabilityData.META,
            OrgOpenroadmPortCapabilityData.META,
            OrgOpenroadmProtEquipmentApsData.META,
            OrgOpenroadmProtOtnLinearApsData.META,
            OrgOpenroadmRoutingData.META,
            OrgOpenroadmRstpData.META,
            OrgOpenroadmSecurityData.META,
            OrgOpenroadmSyslogData.META,
            OrgOpenroadmTelemetryTypesData.META,
            OrgOpenroadmWavelengthMapData.META,

            // network-13.1.1
            OrgOpenroadmAmplifierData.META,
            OrgOpenroadmClliNetworkData.META,
            OrgOpenroadmCommonNetworkData.META,
            OrgOpenroadmDegreeData.META,
            OrgOpenroadmExternalPluggableData.META,
            OrgOpenroadmLinkData.META,
            OrgOpenroadmNetworkData.META,
            OrgOpenroadmNetworkTopologyData.META,
            OrgOpenroadmNetworkTopologyTypesData.META,
            OrgOpenroadmNetworkTypesData.META,
            OrgOpenroadmOtnNetworkTopologyData.META,
            OrgOpenroadmRoadmData.META,
            OrgOpenroadmSrgData.META,
            OrgOpenroadmXponderData.META,

            // service-13.1.1
            OrgOpenroadmBerTestData.META,
            OrgOpenroadmCommonBerTestData.META,
            OrgOpenroadmCommonServiceTypesData.META,
            OrgOpenroadmControllerCustomizationData.META,
            OrgOpenroadmOperationalModeCatalogData.META,
            OrgOpenroadmRoutingConstraintsData.META,
            OrgOpenroadmServiceData.META,
            OrgOpenroadmTopologyData.META);
    }
}
