/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class GroupingTest {
    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadModules(getClass().getResource("/model").toURI());
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
                refineLeaf = (LeafSchemaNode) destination.getDataChildByName(value.getQName());
            } else if ("port".equals(value.getQName().getLocalName())) {
                refineContainer = (ContainerSchemaNode) destination.getDataChildByName(value.getQName());
            } else if ("addresses".equals(value.getQName().getLocalName())) {
                refineList = (ListSchemaNode) destination.getDataChildByName(value.getQName());
            }
        }

        assertNotNull(refineList);
        for (Map.Entry<SchemaPath, SchemaNode> entry : refines.entrySet()) {
            SchemaNode value = entry.getValue();
            if ("id".equals(value.getQName().getLocalName())) {
                refineInnerLeaf = (LeafSchemaNode) refineList.getDataChildByName(value.getQName());
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
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.toString());
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
        assertEquals("description of addresses defined by refine", refineList.getDescription());
        assertEquals("addresses reference added by refine", refineList.getReference());
        assertFalse(refineList.isConfiguration());
        assertEquals(2, (int) refineList.getConstraints().getMinElements());
        assertEquals(Integer.MAX_VALUE, (int) refineList.getConstraints().getMaxElements());

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
        Collection<DataSchemaNode> children = grouping.getChildNodes();
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
        assertEquals(data_g, SchemaNodeUtils.getRootOriginalIfPossible(data_u));

        ChoiceSchemaNode how_u = (ChoiceSchemaNode) destination.getDataChildByName("how");
        assertNotNull(how_u);
        TestUtils.checkIsAddedByUses(how_u, true);
        assertEquals(2, how_u.getCases().size());

        ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping.getDataChildByName("how");
        assertNotNull(how_g);
        TestUtils.checkIsAddedByUses(how_g, false);
        assertEquals(2, how_g.getCases().size());
        assertFalse(how_u.equals(how_g));
        assertEquals(how_g, SchemaNodeUtils.getRootOriginalIfPossible(how_u));

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
        assertEquals(address_g, SchemaNodeUtils.getRootOriginalIfPossible(address_u));

        ContainerSchemaNode port_u = (ContainerSchemaNode) destination.getDataChildByName("port");
        assertNotNull(port_u);
        TestUtils.checkIsAddedByUses(port_u, true);

        ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName("port");
        assertNotNull(port_g);
        TestUtils.checkIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));
        assertEquals(port_g, SchemaNodeUtils.getRootOriginalIfPossible(port_u));

        ListSchemaNode addresses_u = (ListSchemaNode) destination.getDataChildByName("addresses");
        assertNotNull(addresses_u);
        TestUtils.checkIsAddedByUses(addresses_u, true);

        ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName("addresses");
        assertNotNull(addresses_g);
        TestUtils.checkIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));
        assertEquals(addresses_g, SchemaNodeUtils.getRootOriginalIfPossible(addresses_u));

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
        assertEquals(data_g, SchemaNodeUtils.getRootOriginalIfPossible(data_u));

        ChoiceSchemaNode how_u = (ChoiceSchemaNode) foo.getDataChildByName("how");
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

        ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping.getDataChildByName("how");
        assertNotNull(how_g);
        TestUtils.checkIsAddedByUses(how_g, false);
        assertFalse(how_u.equals(how_g));
        assertEquals(how_g, SchemaNodeUtils.getRootOriginalIfPossible(how_u));

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
        assertEquals(address_g, SchemaNodeUtils.getRootOriginalIfPossible(address_u));

        ContainerSchemaNode port_u = (ContainerSchemaNode) foo.getDataChildByName("port");
        assertNotNull(port_u);
        TestUtils.checkIsAddedByUses(port_u, true);

        ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName("port");
        assertNotNull(port_g);
        TestUtils.checkIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));
        assertEquals(port_g, SchemaNodeUtils.getRootOriginalIfPossible(port_u));

        ListSchemaNode addresses_u = (ListSchemaNode) foo.getDataChildByName("addresses");
        assertNotNull(addresses_u);
        TestUtils.checkIsAddedByUses(addresses_u, true);

        ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName("addresses");
        assertNotNull(addresses_g);
        TestUtils.checkIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));
        assertEquals(addresses_g, SchemaNodeUtils.getRootOriginalIfPossible(addresses_u));

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
        Collection<DataSchemaNode> children = augment.getChildNodes();
        assertEquals(1, children.size());
        DataSchemaNode leaf = children.iterator().next();
        assertTrue(leaf instanceof LeafSchemaNode);
        assertEquals("name", leaf.getQName().getLocalName());
    }

    @Test
    public void testCascadeUses() throws ReactorException, ParseException {
        modules = TestUtils.loadModules(Collections.singletonList(getClass().getResourceAsStream(
                "/grouping-test/cascade-uses.yang")));
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
        Date expectedRev = new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-18");
        String expectedPref = "cu";
        SchemaPath expectedPath;

        // grouping-U
        Collection<DataSchemaNode> childNodes = gu.getChildNodes();
        assertEquals(7, childNodes.size());

        LeafSchemaNode leafGroupingU = (LeafSchemaNode) gu.getDataChildByName("leaf-grouping-U");
        assertNotNull(leafGroupingU);
        assertFalse(leafGroupingU.isAddedByUses());
        assertFalse(SchemaNodeUtils.getOriginalIfPossible(leafGroupingU).isPresent());

        for (DataSchemaNode childNode : childNodes) {
            if (!(childNode.getQName().equals(leafGroupingU.getQName()))) {
                TestUtils.checkIsAddedByUses(childNode, true);
            }
        }

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

        // TEST getOriginal from grouping-U
        assertEquals(gv.getDataChildByName("leaf-grouping-V"),
                SchemaNodeUtils.getRootOriginalIfPossible(gu.getDataChildByName("leaf-grouping-V")));
        containerGroupingV = (ContainerSchemaNode) gu.getDataChildByName("container-grouping-V");
        assertEquals(gv.getDataChildByName("container-grouping-V"),
                SchemaNodeUtils.getRootOriginalIfPossible(containerGroupingV));
        assertEquals(gx.getDataChildByName("leaf-grouping-X"),
                SchemaNodeUtils.getRootOriginalIfPossible(containerGroupingV.getDataChildByName("leaf-grouping-X")));
        assertEquals(gy.getDataChildByName("leaf-grouping-Y"),
                SchemaNodeUtils.getRootOriginalIfPossible(containerGroupingV.getDataChildByName("leaf-grouping-Y")));

        assertEquals(gz.getDataChildByName("leaf-grouping-Z"),
                SchemaNodeUtils.getRootOriginalIfPossible(gu.getDataChildByName("leaf-grouping-Z")));
        assertEquals(gzz.getDataChildByName("leaf-grouping-ZZ"),
                SchemaNodeUtils.getRootOriginalIfPossible(gu.getDataChildByName("leaf-grouping-ZZ")));

        // TEST getOriginal from grouping-V
        assertEquals(gz.getDataChildByName("leaf-grouping-Z"),
                SchemaNodeUtils.getRootOriginalIfPossible(gv.getDataChildByName("leaf-grouping-Z")));
        assertEquals(gzz.getDataChildByName("leaf-grouping-ZZ"),
                SchemaNodeUtils.getRootOriginalIfPossible(gv.getDataChildByName("leaf-grouping-ZZ")));

        // TEST getOriginal from grouping-X
        assertEquals(gy.getDataChildByName("leaf-grouping-Y"),
                SchemaNodeUtils.getRootOriginalIfPossible(gx.getDataChildByName("leaf-grouping-Y")));
    }

    @Test
    public void testAddedByUsesLeafTypeQName() throws URISyntaxException, ReactorException {

        Set<Module> loadModules = TestUtils.loadModules(getClass().getResource("/added-by-uses-leaf-test").toURI());

        assertEquals(2, loadModules.size());

        Module foo = null;
        Module imp = null;
        for (Module module : loadModules) {
            if (module.getName().equals("foo")) {
                foo = module;
            }
            if (module.getName().equals("import-module")) {
                imp = module;
            }
        }

        LeafSchemaNode leaf = (LeafSchemaNode) ((ContainerSchemaNode) foo.getDataChildByName("my-container"))
                .getDataChildByName("my-leaf");

        TypeDefinition<?> impType = null;
        Set<TypeDefinition<?>> typeDefinitions = imp.getTypeDefinitions();
        for (TypeDefinition<?> typeDefinition : typeDefinitions) {
            if (typeDefinition.getQName().getLocalName().equals("imp-type")) {
                impType = typeDefinition;
                break;
            }
        }

        assertEquals(leaf.getType().getQName(), impType.getQName());
    }
}
