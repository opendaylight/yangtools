/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javassist.ClassPool;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.isis.topology.rev131021.IgpNodeAttributes1;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.nt.l3.unicast.igp.topology.rev131021.Node1;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.RuntimeGeneratedMappingServiceImpl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

public class RestconfUtilsTest {

    private RuntimeGeneratedMappingServiceImpl mappingService;

    @Before
    public void setup() {
        this.mappingService = new RuntimeGeneratedMappingServiceImpl(new ClassPool());

        final ModuleInfoBackedContext moduleInfo = ModuleInfoBackedContext.create();
        moduleInfo.addModuleInfos(BindingReflections.loadModuleInfos());
        this.mappingService.onGlobalContextUpdated(moduleInfo.tryToCreateSchemaContext().get());
    }

    @Test
    public void firstTest() { // test static state collisions with the other test
        final InstanceIdentifier<Topology> instanceIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("example-pcep-topology"))).build();
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology-bug1196-linux.xml");
        RestconfUtils.toRestconfIdentifier(instanceIdentifier, this.mappingService,
                this.mappingService.getSchemaContext()).getValue();
    }

    @Test
    public void testToDataObjectMappingWithNestedAugmentations() {
        final InstanceIdentifier<Topology> topologyIdentifier = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class).build();
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("topology.xml");
        final DataSchemaNode dataSchema = RestconfUtils.toRestconfIdentifier(topologyIdentifier, this.mappingService,
                this.mappingService.getSchemaContext()).getValue();
        final Topology topology = (Topology) RestconfUtils.dataObjectFromInputStream(topologyIdentifier, is,
                this.mappingService.getSchemaContext(), this.mappingService, dataSchema);

        assertNotNull(topology);
        assertEquals(1, topology.getNode().size());
        final Node node = topology.getNode().get(0);
        assertEquals("bgpls://IsisLevel2:1/type=node&as=72&domain=673720360&router=0000.0000.0042", node.getNodeId()
                .getValue());

        final Node1 node1 = node.getAugmentation(Node1.class);
        assertNotNull(node1);
        assertNotNull(node1.getIgpNodeAttributes());
        assertEquals("Of-9k-02", node1.getIgpNodeAttributes().getName().getValue());

        final IgpNodeAttributes1 igpAttributes1 = node1.getIgpNodeAttributes()
                .getAugmentation(IgpNodeAttributes1.class);
        assertNotNull(igpAttributes1);
        assertNotNull(igpAttributes1.getIsisNodeAttributes());
        assertEquals("0000.1111.2222", igpAttributes1.getIsisNodeAttributes().getIso().getIsoSystemId().getValue());
    }

}
