/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;

public class GroupingTest {
    private Set<Module> modules;

    @Before
    public void init() throws FileNotFoundException {
        modules = TestUtils.loadModules(getClass().getResource("/model").getPath());
        assertEquals(3, modules.size());
    }

    @Test
    public void testRefine() {
        Module testModule = TestUtils.findModule(modules, "foo");
        ContainerSchemaNode peer = (ContainerSchemaNode) testModule.getDataChildByName("peer");
        ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName("destination");

        Set<UsesNode> usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        UsesNode usesNode = usesNodes.iterator().next();
        Map<SchemaPath, SchemaNode> refines = usesNode.getRefines();
        assertEquals(4, refines.size());

        LeafSchemaNode refineLeaf = null;
        ContainerSchemaNode refineContainer = null;
        ListSchemaNode refineList = null;
        LeafSchemaNode refineInnerLeaf = null;
        for (Map.Entry<SchemaPath, SchemaNode> entry : refines.entrySet()) {
            SchemaNode value = entry.getValue();
            if ("address".equals(value.getQName().getLocalName())) {
                refineLeaf = (LeafSchemaNode) value;
            } else if ("port".equals(value.getQName().getLocalName())) {
                refineContainer = (ContainerSchemaNode) value;
            } else if ("addresses".equals(value.getQName().getLocalName())) {
                refineList = (ListSchemaNode) value;
            } else if ("id".equals(value.getQName().getLocalName())) {
                refineInnerLeaf = (LeafSchemaNode) value;
            }
        }

        // leaf address
        assertNotNull(refineLeaf);
        assertEquals("IP address of target node", refineLeaf.getDescription());
        assertEquals("address reference added by refine", refineLeaf.getReference());
        assertFalse(refineLeaf.isConfiguration());
        assertFalse(refineLeaf.getConstraints().isMandatory());
        Set<MustDefinition> leafMustConstraints = refineLeaf.getConstraints().getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        MustDefinition leafMust = leafMustConstraints.iterator().next();
        assertEquals("\"ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)\"", leafMust.toString());
        assertEquals(1, refineLeaf.getUnknownSchemaNodes().size());

        // container port
        assertNotNull(refineContainer);
        Set<MustDefinition> mustConstraints = refineContainer.getConstraints().getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals("description of port defined by refine", refineContainer.getDescription());
        assertEquals("port reference added by refine", refineContainer.getReference());
        assertFalse(refineContainer.isConfiguration());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        assertNotNull(refineList);
        assertEquals("description of addresses defined by refine", refineList.getDescription());
        assertEquals("addresses reference added by refine", refineList.getReference());
        assertFalse(refineList.isConfiguration());
        assertEquals(2, (int) refineList.getConstraints().getMinElements());
        assertEquals(12, (int) refineList.getConstraints().getMaxElements());

        // leaf id
        assertNotNull(refineInnerLeaf);
        assertEquals("id of address", refineInnerLeaf.getDescription());
    }

    @Test
    public void testGrouping() {
        Module testModule = TestUtils.findModule(modules, "baz");
        Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());
        GroupingDefinition grouping = groupings.iterator().next();
        Set<DataSchemaNode> children = grouping.getChildNodes();
        assertEquals(5, children.size());
    }

    @Test
    public void testUses() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping

        Module testModule = TestUtils.findModule(modules, "baz");

        // get grouping
        Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());
        GroupingDefinition grouping = groupings.iterator().next();

        testModule = TestUtils.findModule(modules, "foo");

        // get node containing uses
        ContainerSchemaNode peer = (ContainerSchemaNode) testModule.getDataChildByName("peer");
        ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName("destination");

        // check uses
        Set<UsesNode> uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        AnyXmlSchemaNode data_u = (AnyXmlSchemaNode) destination.getDataChildByName("data");
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        AnyXmlSchemaNode data_g = (AnyXmlSchemaNode) grouping.getDataChildByName("data");
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));

        ChoiceNode how_u = (ChoiceNode) destination.getDataChildByName("how");
        assertNotNull(how_u);
        TestUtils.checkIsAddedByUses(how_u, true);
        assertEquals(2, how_u.getCases().size());

        ChoiceNode how_g = (ChoiceNode) grouping.getDataChildByName("how");
        assertNotNull(how_g);
        TestUtils.checkIsAddedByUses(how_g, false);
        assertEquals(2, how_g.getCases().size());
        assertFalse(how_u.equals(how_g));

        LeafSchemaNode address_u = (LeafSchemaNode) destination.getDataChildByName("address");
        assertNotNull(address_u);
        assertEquals("1.2.3.4", address_u.getDefault());
        assertEquals("IP address of target node", address_u.getDescription());
        assertEquals("address reference added by refine", address_u.getReference());
        assertFalse(address_u.isConfiguration());
        assertTrue(address_u.isAddedByUses());
        assertFalse(address_u.getConstraints().isMandatory());

        LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName("address");
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertNull(address_g.getDefault());
        assertEquals("Target IP address", address_g.getDescription());
        assertNull(address_g.getReference());
        assertTrue(address_g.isConfiguration());
        assertFalse(address_u.equals(address_g));
        assertTrue(address_g.getConstraints().isMandatory());

        ContainerSchemaNode port_u = (ContainerSchemaNode) destination.getDataChildByName("port");
        assertNotNull(port_u);
        TestUtils.checkIsAddedByUses(port_u, true);

        ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName("port");
        assertNotNull(port_g);
        TestUtils.checkIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));

        ListSchemaNode addresses_u = (ListSchemaNode) destination.getDataChildByName("addresses");
        assertNotNull(addresses_u);
        TestUtils.checkIsAddedByUses(addresses_u, true);

        ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName("addresses");
        assertNotNull(addresses_g);
        TestUtils.checkIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));

        // grouping defined by 'uses'
        Set<GroupingDefinition> groupings_u = destination.getGroupings();
        assertEquals(1, groupings_u.size());
        GroupingDefinition grouping_u = groupings_u.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_u, true);

        // grouping defined in 'grouping' node
        Set<GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        GroupingDefinition grouping_g = groupings_g.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_g, false);
        assertFalse(grouping_u.equals(grouping_g));

        List<UnknownSchemaNode> nodes_u = destination.getUnknownSchemaNodes();
        assertEquals(1, nodes_u.size());
        UnknownSchemaNode node_u = nodes_u.get(0);
        assertTrue(node_u.isAddedByUses());

        List<UnknownSchemaNode> nodes_g = grouping.getUnknownSchemaNodes();
        assertEquals(1, nodes_g.size());
        UnknownSchemaNode node_g = nodes_g.get(0);
        assertFalse(node_g.isAddedByUses());
        assertFalse(node_u.equals(node_g));
    }

    @Test
    public void testUsesUnderModule() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping

        Module testModule = TestUtils.findModule(modules, "baz");

        // get grouping
        Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());
        GroupingDefinition grouping = groupings.iterator().next();

        // get node containing uses
        Module foo = TestUtils.findModule(modules, "foo");

        // check uses
        Set<UsesNode> uses = foo.getUses();
        assertEquals(1, uses.size());

        // check uses process
        AnyXmlSchemaNode data_u = (AnyXmlSchemaNode) foo.getDataChildByName("data");
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        AnyXmlSchemaNode data_g = (AnyXmlSchemaNode) grouping.getDataChildByName("data");
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));

        ChoiceNode how_u = (ChoiceNode) foo.getDataChildByName("how");
        assertNotNull(how_u);
        TestUtils.checkIsAddedByUses(how_u, true);
        assertFalse(how_u.isAugmenting());
        Set<ChoiceCaseNode> cases_u = how_u.getCases();
        assertEquals(2, cases_u.size());
        ChoiceCaseNode interval = how_u.getCaseNodeByName("interval");
        assertFalse(interval.isAugmenting());
        LeafSchemaNode name = (LeafSchemaNode) interval.getDataChildByName("name");
        assertTrue(name.isAugmenting());
        LeafSchemaNode intervalLeaf = (LeafSchemaNode) interval.getDataChildByName("interval");
        assertFalse(intervalLeaf.isAugmenting());

        ChoiceNode how_g = (ChoiceNode) grouping.getDataChildByName("how");
        assertNotNull(how_g);
        TestUtils.checkIsAddedByUses(how_g, false);
        assertFalse(how_u.equals(how_g));

        LeafSchemaNode address_u = (LeafSchemaNode) foo.getDataChildByName("address");
        assertNotNull(address_u);
        assertNull(address_u.getDefault());
        assertEquals("Target IP address", address_u.getDescription());
        assertNull(address_u.getReference());
        assertTrue(address_u.isConfiguration());
        assertTrue(address_u.isAddedByUses());

        LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName("address");
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertNull(address_g.getDefault());
        assertEquals("Target IP address", address_g.getDescription());
        assertNull(address_g.getReference());
        assertTrue(address_g.isConfiguration());
        assertFalse(address_u.equals(address_g));

        ContainerSchemaNode port_u = (ContainerSchemaNode) foo.getDataChildByName("port");
        assertNotNull(port_u);
        TestUtils.checkIsAddedByUses(port_u, true);

        ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName("port");
        assertNotNull(port_g);
        TestUtils.checkIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));

        ListSchemaNode addresses_u = (ListSchemaNode) foo.getDataChildByName("addresses");
        assertNotNull(addresses_u);
        TestUtils.checkIsAddedByUses(addresses_u, true);

        ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName("addresses");
        assertNotNull(addresses_g);
        TestUtils.checkIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));

        // grouping defined by 'uses'
        Set<GroupingDefinition> groupings_u = foo.getGroupings();
        assertEquals(1, groupings_u.size());
        GroupingDefinition grouping_u = groupings_u.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_u, true);

        // grouping defined in 'grouping' node
        Set<GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        GroupingDefinition grouping_g = groupings_g.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_g, false);
        assertFalse(grouping_u.equals(grouping_g));

        List<UnknownSchemaNode> nodes_u = foo.getUnknownSchemaNodes();
        assertEquals(1, nodes_u.size());
        UnknownSchemaNode node_u = nodes_u.get(0);
        assertTrue(node_u.isAddedByUses());

        List<UnknownSchemaNode> nodes_g = grouping.getUnknownSchemaNodes();
        assertEquals(1, nodes_g.size());
        UnknownSchemaNode node_g = nodes_g.get(0);
        assertFalse(node_g.isAddedByUses());
        assertFalse(node_u.equals(node_g));

        UsesNode un = uses.iterator().next();
        Set<AugmentationSchema> usesAugments = un.getAugmentations();
        assertEquals(1, usesAugments.size());
        AugmentationSchema augment = usesAugments.iterator().next();
        assertEquals("inner augment", augment.getDescription());
        Set<DataSchemaNode> children = augment.getChildNodes();
        assertEquals(1, children.size());
        DataSchemaNode leaf = children.iterator().next();
        assertTrue(leaf instanceof LeafSchemaNode);
        assertEquals("name", leaf.getQName().getLocalName());
    }

    @Test
    public void testCascadeUses() throws FileNotFoundException, ParseException {
        File yangFile = new File(getClass().getResource("/grouping-test/cascade-uses.yang").getPath());
        YangModelParser parser = new YangParserImpl();
        modules = parser.parseYangModels(Collections.singletonList(yangFile));
        assertEquals(1, modules.size());

        Module testModule = TestUtils.findModule(modules, "cascade-uses");
        Set<GroupingDefinition> groupings = testModule.getGroupings();

        GroupingDefinition gu = null;
        GroupingDefinition gv = null;
        GroupingDefinition gx = null;
        GroupingDefinition gy = null;
        GroupingDefinition gz = null;
        GroupingDefinition gzz = null;
        for (GroupingDefinition gd : groupings) {
            String name = gd.getQName().getLocalName();
            switch (name) {
            case "grouping-U":
                gu = gd;
                break;
            case "grouping-V":
                gv = gd;
                break;
            case "grouping-X":
                gx = gd;
                break;
            case "grouping-Y":
                gy = gd;
                break;
            case "grouping-Z":
                gz = gd;
                break;
            case "grouping-ZZ":
                gzz = gd;
                break;
            }
        }
        assertNotNull(gu);
        assertNotNull(gv);
        assertNotNull(gx);
        assertNotNull(gy);
        assertNotNull(gz);
        assertNotNull(gzz);

        URI expectedNS = URI.create("urn:grouping:cascade-uses");
        Date expectedRev = TestUtils.simpleDateFormat.parse("2013-07-18");
        String expectedPref = "cu";
        SchemaPath expectedPath;

        // grouping-U
        Set<DataSchemaNode> childNodes = gu.getChildNodes();
        assertEquals(7, childNodes.size());
        LeafSchemaNode leafGroupingU = null;
        for (DataSchemaNode childNode : childNodes) {
            if ("leaf-grouping-U".equals(childNode.getQName().getLocalName())) {
                leafGroupingU = (LeafSchemaNode) childNode;
            } else {
                TestUtils.checkIsAddedByUses(childNode, true);
            }
        }
        assertNotNull(leafGroupingU);
        assertFalse(leafGroupingU.isAddedByUses());

        // grouping-V
        childNodes = gv.getChildNodes();
        assertEquals(4, childNodes.size());
        LeafSchemaNode leafGroupingV = null;
        ContainerSchemaNode containerGroupingV = null;
        for (DataSchemaNode childNode : childNodes) {
            if ("leaf-grouping-V".equals(childNode.getQName().getLocalName())) {
                leafGroupingV = (LeafSchemaNode) childNode;
            } else if ("container-grouping-V".equals(childNode.getQName().getLocalName())) {
                containerGroupingV = (ContainerSchemaNode) childNode;
            } else {
                TestUtils.checkIsAddedByUses(childNode, true);
            }
        }
        assertNotNull(leafGroupingV);
        assertFalse(leafGroupingV.isAddedByUses());

        // grouping-V/container-grouping-V
        assertNotNull(containerGroupingV);
        assertFalse(containerGroupingV.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-V",
                "container-grouping-V");
        assertEquals(expectedPath, containerGroupingV.getPath());
        childNodes = containerGroupingV.getChildNodes();
        assertEquals(2, childNodes.size());
        for (DataSchemaNode childNode : childNodes) {
            TestUtils.checkIsAddedByUses(childNode, true);
        }

        // grouping-V/container-grouping-V/leaf-grouping-X
        LeafSchemaNode leafXinContainerV = (LeafSchemaNode) containerGroupingV.getDataChildByName("leaf-grouping-X");
        assertNotNull(leafXinContainerV);
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-V",
                "container-grouping-V", "leaf-grouping-X");
        assertEquals(expectedPath, leafXinContainerV.getPath());
        // grouping-V/container-grouping-V/leaf-grouping-Y
        LeafSchemaNode leafYinContainerV = (LeafSchemaNode) containerGroupingV.getDataChildByName("leaf-grouping-Y");
        assertNotNull(leafYinContainerV);
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-V",
                "container-grouping-V", "leaf-grouping-Y");
        assertEquals(expectedPath, leafYinContainerV.getPath());

        // grouping-X
        childNodes = gx.getChildNodes();
        assertEquals(2, childNodes.size());

        // grouping-X/leaf-grouping-X
        LeafSchemaNode leafXinGX = (LeafSchemaNode) gx.getDataChildByName("leaf-grouping-X");
        assertNotNull(leafXinGX);
        assertFalse(leafXinGX.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-X",
                "leaf-grouping-X");
        assertEquals(expectedPath, leafXinGX.getPath());

        // grouping-X/leaf-grouping-Y
        LeafSchemaNode leafYinGX = (LeafSchemaNode) gx.getDataChildByName("leaf-grouping-Y");
        assertNotNull(leafYinGX);
        assertTrue(leafYinGX.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-X",
                "leaf-grouping-Y");
        assertEquals(expectedPath, leafYinGX.getPath());

        // grouping-Y
        childNodes = gy.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Y/leaf-grouping-Y
        LeafSchemaNode leafYinGY = (LeafSchemaNode) gy.getDataChildByName("leaf-grouping-Y");
        assertNotNull(leafYinGY);
        assertFalse(leafYinGY.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-Y",
                "leaf-grouping-Y");
        assertEquals(expectedPath, leafYinGY.getPath());

        // grouping-Z
        childNodes = gz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Z/leaf-grouping-Z
        LeafSchemaNode leafZinGZ = (LeafSchemaNode) gz.getDataChildByName("leaf-grouping-Z");
        assertNotNull(leafZinGZ);
        assertFalse(leafZinGZ.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-Z",
                "leaf-grouping-Z");
        assertEquals(expectedPath, leafZinGZ.getPath());

        // grouping-ZZ
        childNodes = gzz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-ZZ/leaf-grouping-ZZ
        LeafSchemaNode leafZZinGZZ = (LeafSchemaNode) gzz.getDataChildByName("leaf-grouping-ZZ");
        assertNotNull(leafZZinGZZ);
        assertFalse(leafZZinGZZ.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedNS, expectedRev, expectedPref, "grouping-ZZ",
                "leaf-grouping-ZZ");
        assertEquals(expectedPath, leafZZinGZZ.getPath());
    }

}
