/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import javassist.ClassPool;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.ReportedLsp1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.Lsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.vs.tlv.VsTlv;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.pcep.rev131024.Node1;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class Bug1196Test {

    private RuntimeGeneratedMappingServiceImpl mappingService;

    @Before
    public void setup() {
        this.mappingService = new RuntimeGeneratedMappingServiceImpl(new ClassPool());

        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        this.mappingService.onGlobalContextUpdated(moduleInfo.tryToCreateSchemaContext().get());
    }

    @Test
    public void testXmlDataToDataObjectLinuxCase() {
        final InstanceIdentifier<Topology> instanceIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("example-pcep-topology"))).toInstance();
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology-bug1196-linux.xml");
        final DataSchemaNode dataSchema = RestconfUtils.toRestconfIdentifier(instanceIdentifier, this.mappingService,
                this.mappingService.getSchemaContext()).getValue();
        Topology topology = (Topology) RestconfUtils.dataObjectFromInputStream(instanceIdentifier, is,
                this.mappingService.getSchemaContext(), this.mappingService, dataSchema);
        assertNotNull(topology);
        assertNotNull(topology.getNode());
        assertEquals(1, topology.getNode().size());
        Node node = topology.getNode().get(0);
        Node1 node1 = node.getAugmentation(Node1.class);
        assertNotNull(node1);
        final Lsp lsp = node1.getPathComputationClient().getReportedLsp().get(0).getAugmentation(ReportedLsp1.class).getLsp();
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.Tlvs tlvs = lsp.getTlvs();
        assertNotNull(tlvs);
        VsTlv vsTlv = tlvs.getVsTlv();
        assertNotNull(vsTlv.getVendorPayload());
    }

    @Test
    public void testXmlDataToDataObjectUnixCase() {
        final InstanceIdentifier<Topology> instanceIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("example-pcep-topology"))).toInstance();
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology-bug1196-unix.xml");
        final DataSchemaNode dataSchema = RestconfUtils.toRestconfIdentifier(instanceIdentifier, this.mappingService,
                this.mappingService.getSchemaContext()).getValue();
        Topology topology = (Topology) RestconfUtils.dataObjectFromInputStream(instanceIdentifier, is,
                this.mappingService.getSchemaContext(), this.mappingService, dataSchema);
        assertNotNull(topology);
        assertNotNull(topology.getNode());
        assertEquals(1, topology.getNode().size());
        Node node = topology.getNode().get(0);
        Node1 node1 = node.getAugmentation(Node1.class);
        assertNotNull(node1);
        final Lsp lsp = node1.getPathComputationClient().getReportedLsp().get(0).getAugmentation(ReportedLsp1.class).getLsp();
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.Tlvs tlvs = lsp.getTlvs();
        assertNotNull(tlvs);
        VsTlv vsTlv = tlvs.getVsTlv();
        assertNotNull(vsTlv.getVendorPayload());
    }

}
