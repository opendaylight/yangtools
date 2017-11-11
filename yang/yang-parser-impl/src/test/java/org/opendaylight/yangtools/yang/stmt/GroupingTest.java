/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class GroupingTest {
    private SchemaContext ctx;
    private Module foo;
    private Module baz;

    @Before
    public void init() throws Exception {
        ctx = TestUtils.loadModules(getClass().getResource("/model").toURI());
        foo = TestUtils.findModule(ctx, "foo").get();
        baz = TestUtils.findModule(ctx, "baz").get();
        assertEquals(3, ctx.getModules().size());
    }

    @Test
    public void testRefine() {
        final Module testModule = TestUtils.findModule(ctx, "foo").get();
        final ContainerSchemaNode peer = (ContainerSchemaNode) testModule.getDataChildByName(QName.create(
                testModule.getQNameModule(), "peer"));
        final ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                testModule.getQNameModule(), "destination"));

        final Set<UsesNode> usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        final UsesNode usesNode = usesNodes.iterator().next();
        final Map<SchemaPath, SchemaNode> refines = usesNode.getRefines();
        assertEquals(4, refines.size());

        LeafSchemaNode refineLeaf = null;
        ContainerSchemaNode refineContainer = null;
        ListSchemaNode refineList = null;
        LeafSchemaNode refineInnerLeaf = null;
        for (final Map.Entry<SchemaPath, SchemaNode> entry : refines.entrySet()) {
            final SchemaNode value = entry.getValue();
            if ("address".equals(value.getQName().getLocalName())) {
                refineLeaf = (LeafSchemaNode) destination.getDataChildByName(value.getQName());
            } else if ("port".equals(value.getQName().getLocalName())) {
                refineContainer = (ContainerSchemaNode) destination.getDataChildByName(value.getQName());
            } else if ("addresses".equals(value.getQName().getLocalName())) {
                refineList = (ListSchemaNode) destination.getDataChildByName(value.getQName());
            }
        }

        assertNotNull(refineList);
        for (final Map.Entry<SchemaPath, SchemaNode> entry : refines.entrySet()) {
            final SchemaNode value = entry.getValue();
            if ("id".equals(value.getQName().getLocalName())) {
                refineInnerLeaf = (LeafSchemaNode) refineList.getDataChildByName(value.getQName());
            }
        }

        // leaf address
        assertNotNull(refineLeaf);
        assertEquals(Optional.of("IP address of target node"), refineLeaf.getDescription());
        assertEquals(Optional.of("address reference added by refine"), refineLeaf.getReference());
        assertFalse(refineLeaf.isConfiguration());
        assertFalse(refineLeaf.isMandatory());
        final Collection<MustDefinition> leafMustConstraints = refineLeaf.getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        final MustDefinition leafMust = leafMustConstraints.iterator().next();
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.toString());
        assertEquals(1, refineLeaf.getUnknownSchemaNodes().size());

        // container port
        assertNotNull(refineContainer);
        final Collection<MustDefinition> mustConstraints = refineContainer.getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals(Optional.of("description of port defined by refine"), refineContainer.getDescription());
        assertEquals(Optional.of("port reference added by refine"), refineContainer.getReference());
        assertFalse(refineContainer.isConfiguration());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        assertEquals(Optional.of("description of addresses defined by refine"), refineList.getDescription());
        assertEquals(Optional.of("addresses reference added by refine"), refineList.getReference());
        assertFalse(refineList.isConfiguration());

        final ElementCountConstraint constraint = refineList.getElementCountConstraint().get();
        assertEquals(2, constraint.getMinElements().intValue());
        assertNull(constraint.getMaxElements());

        // leaf id
        assertNotNull(refineInnerLeaf);
        assertEquals(Optional.of("id of address"), refineInnerLeaf.getDescription());
    }

    @Test
    public void testGrouping() {
        final Module testModule = TestUtils.findModule(ctx, "baz").get();
        final Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();
        final Collection<DataSchemaNode> children = grouping.getChildNodes();
        assertEquals(5, children.size());
    }

    @Test
    public void testUses() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping


        // get grouping
        final Set<GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();

        // get node containing uses
        final ContainerSchemaNode peer = (ContainerSchemaNode) foo.getDataChildByName(QName.create(
                foo.getQNameModule(), "peer"));
        final ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                foo.getQNameModule(), "destination"));

        // check uses
        final Set<UsesNode> uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final AnyXmlSchemaNode data_u = (AnyXmlSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "data"));
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        final AnyXmlSchemaNode data_g = (AnyXmlSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "data"));
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));
        assertEquals(data_g, SchemaNodeUtils.getRootOriginalIfPossible(data_u));

        final ChoiceSchemaNode how_u = (ChoiceSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "how"));
        assertNotNull(how_u);
        TestUtils.checkIsAddedByUses(how_u, true);
        assertEquals(2, how_u.getCases().size());

        final ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "how"));
        assertNotNull(how_g);
        TestUtils.checkIsAddedByUses(how_g, false);
        assertEquals(2, how_g.getCases().size());
        assertFalse(how_u.equals(how_g));
        assertEquals(how_g, SchemaNodeUtils.getRootOriginalIfPossible(how_u));

        final LeafSchemaNode address_u = (LeafSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "address"));
        assertNotNull(address_u);
        assertEquals(Optional.of("1.2.3.4"), address_u.getType().getDefaultValue());
        assertEquals(Optional.of("IP address of target node"), address_u.getDescription());
        assertEquals(Optional.of("address reference added by refine"), address_u.getReference());
        assertFalse(address_u.isConfiguration());
        assertTrue(address_u.isAddedByUses());
        assertFalse(address_u.isMandatory());

        final LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "address"));
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertEquals(Optional.empty(), address_g.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_g.getDescription());
        assertFalse(address_g.getReference().isPresent());
        assertTrue(address_g.isConfiguration());
        assertFalse(address_u.equals(address_g));
        assertTrue(address_g.isMandatory());
        assertEquals(address_g, SchemaNodeUtils.getRootOriginalIfPossible(address_u));

        final ContainerSchemaNode port_u = (ContainerSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "port"));
        assertNotNull(port_u);
        TestUtils.checkIsAddedByUses(port_u, true);

        final ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "port"));
        assertNotNull(port_g);
        TestUtils.checkIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));
        assertEquals(port_g, SchemaNodeUtils.getRootOriginalIfPossible(port_u));

        final ListSchemaNode addresses_u = (ListSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "addresses"));
        assertNotNull(addresses_u);
        TestUtils.checkIsAddedByUses(addresses_u, true);

        final ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "addresses"));
        assertNotNull(addresses_g);
        TestUtils.checkIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));
        assertEquals(addresses_g, SchemaNodeUtils.getRootOriginalIfPossible(addresses_u));

        // grouping defined by 'uses'
        final Set<GroupingDefinition> groupings_u = destination.getGroupings();
        assertEquals(1, groupings_u.size());
        final GroupingDefinition grouping_u = groupings_u.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_u, true);

        // grouping defined in 'grouping' node
        final Set<GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final GroupingDefinition grouping_g = groupings_g.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_g, false);
        assertFalse(grouping_u.equals(grouping_g));

        final List<UnknownSchemaNode> nodes_u = destination.getUnknownSchemaNodes();
        assertEquals(1, nodes_u.size());
        final UnknownSchemaNode node_u = nodes_u.get(0);
        assertTrue(node_u.isAddedByUses());

        final List<UnknownSchemaNode> nodes_g = grouping.getUnknownSchemaNodes();
        assertEquals(1, nodes_g.size());
        final UnknownSchemaNode node_g = nodes_g.get(0);
        assertFalse(node_g.isAddedByUses());
        assertFalse(node_u.equals(node_g));
    }

    @Test
    public void testUsesUnderModule() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping

        // get grouping
        final Set<GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();

        // check uses
        final Set<UsesNode> uses = foo.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final AnyXmlSchemaNode data_u = (AnyXmlSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "data"));
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        final AnyXmlSchemaNode data_g = (AnyXmlSchemaNode) grouping.getDataChildByName(QName.create(
            baz.getQNameModule(), "data"));
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));
        assertEquals(data_g, SchemaNodeUtils.getRootOriginalIfPossible(data_u));

        final ChoiceSchemaNode how_u = (ChoiceSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "how"));
        assertNotNull(how_u);
        TestUtils.checkIsAddedByUses(how_u, true);
        assertFalse(how_u.isAugmenting());
        final SortedMap<QName, ChoiceCaseNode> cases_u = how_u.getCases();
        assertEquals(2, cases_u.size());
        final ChoiceCaseNode interval = how_u.findCaseNodes("interval").iterator().next();
        assertFalse(interval.isAugmenting());
        final LeafSchemaNode name = (LeafSchemaNode) interval.getDataChildByName(QName.create(foo.getQNameModule(),
                "name"));
        assertTrue(name.isAugmenting());
        final LeafSchemaNode intervalLeaf = (LeafSchemaNode) interval.getDataChildByName(QName.create(
                foo.getQNameModule(), "interval"));
        assertFalse(intervalLeaf.isAugmenting());

        final ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "how"));
        assertNotNull(how_g);
        TestUtils.checkIsAddedByUses(how_g, false);
        assertFalse(how_u.equals(how_g));
        assertEquals(how_g, SchemaNodeUtils.getRootOriginalIfPossible(how_u));

        final LeafSchemaNode address_u = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "address"));
        assertNotNull(address_u);
        assertEquals(Optional.empty(), address_u.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_u.getDescription());
        assertFalse(address_u.getReference().isPresent());
        assertTrue(address_u.isConfiguration());
        assertTrue(address_u.isAddedByUses());

        final LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "address"));
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertEquals(Optional.empty(), address_g.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_g.getDescription());
        assertFalse(address_g.getReference().isPresent());
        assertTrue(address_g.isConfiguration());
        assertFalse(address_u.equals(address_g));
        assertEquals(address_g, SchemaNodeUtils.getRootOriginalIfPossible(address_u));

        final ContainerSchemaNode port_u = (ContainerSchemaNode) foo.getDataChildByName(QName.create(
                foo.getQNameModule(), "port"));
        assertNotNull(port_u);
        TestUtils.checkIsAddedByUses(port_u, true);

        final ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "port"));
        assertNotNull(port_g);
        TestUtils.checkIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));
        assertEquals(port_g, SchemaNodeUtils.getRootOriginalIfPossible(port_u));

        final ListSchemaNode addresses_u = (ListSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "addresses"));
        assertNotNull(addresses_u);
        TestUtils.checkIsAddedByUses(addresses_u, true);

        final ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "addresses"));
        assertNotNull(addresses_g);
        TestUtils.checkIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));
        assertEquals(addresses_g, SchemaNodeUtils.getRootOriginalIfPossible(addresses_u));

        // grouping defined by 'uses'
        final Set<GroupingDefinition> groupings_u = foo.getGroupings();
        assertEquals(1, groupings_u.size());
        final GroupingDefinition grouping_u = groupings_u.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_u, true);

        // grouping defined in 'grouping' node
        final Set<GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final GroupingDefinition grouping_g = groupings_g.iterator().next();
        TestUtils.checkIsAddedByUses(grouping_g, false);
        assertFalse(grouping_u.equals(grouping_g));

        final List<UnknownSchemaNode> nodes_u = foo.getUnknownSchemaNodes();
        assertEquals(1, nodes_u.size());
        final UnknownSchemaNode node_u = nodes_u.get(0);
        assertTrue(node_u.isAddedByUses());

        final List<UnknownSchemaNode> nodes_g = grouping.getUnknownSchemaNodes();
        assertEquals(1, nodes_g.size());
        final UnknownSchemaNode node_g = nodes_g.get(0);
        assertFalse(node_g.isAddedByUses());
        assertFalse(node_u.equals(node_g));

        final UsesNode un = uses.iterator().next();
        final Set<AugmentationSchemaNode> usesAugments = un.getAugmentations();
        assertEquals(1, usesAugments.size());
        final AugmentationSchemaNode augment = usesAugments.iterator().next();
        assertEquals(Optional.of("inner augment"), augment.getDescription());
        final Collection<DataSchemaNode> children = augment.getChildNodes();
        assertEquals(1, children.size());
        final DataSchemaNode leaf = children.iterator().next();
        assertTrue(leaf instanceof LeafSchemaNode);
        assertEquals("name", leaf.getQName().getLocalName());
    }

    @Test
    public void testCascadeUses() throws ReactorException, ParseException, IOException, YangSyntaxErrorException {
        ctx = TestUtils.loadModuleResources(getClass(), "/grouping-test/cascade-uses.yang");
        assertEquals(1, ctx.getModules().size());

        final Module testModule = TestUtils.findModule(ctx, "cascade-uses").get();
        final Set<GroupingDefinition> groupings = testModule.getGroupings();

        GroupingDefinition gu = null;
        GroupingDefinition gv = null;
        GroupingDefinition gx = null;
        GroupingDefinition gy = null;
        GroupingDefinition gz = null;
        GroupingDefinition gzz = null;
        for (final GroupingDefinition gd : groupings) {
            final String name = gd.getQName().getLocalName();
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
                default:
                    break;
            }
        }
        assertNotNull(gu);
        assertNotNull(gv);
        assertNotNull(gx);
        assertNotNull(gy);
        assertNotNull(gz);
        assertNotNull(gzz);

        final QNameModule expectedModule = QNameModule.create(URI.create("urn:grouping:cascade-uses"),
            Revision.of("2013-07-18"));

        // grouping-U
        Collection<DataSchemaNode> childNodes = gu.getChildNodes();
        assertEquals(7, childNodes.size());

        final LeafSchemaNode leafGroupingU = (LeafSchemaNode) gu.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-U"));
        assertNotNull(leafGroupingU);
        assertFalse(leafGroupingU.isAddedByUses());
        assertFalse(SchemaNodeUtils.getOriginalIfPossible(leafGroupingU).isPresent());

        for (final DataSchemaNode childNode : childNodes) {
            if (!childNode.getQName().equals(leafGroupingU.getQName())) {
                TestUtils.checkIsAddedByUses(childNode, true);
            }
        }

        // grouping-V
        childNodes = gv.getChildNodes();
        assertEquals(4, childNodes.size());
        LeafSchemaNode leafGroupingV = null;
        ContainerSchemaNode containerGroupingV = null;
        for (final DataSchemaNode childNode : childNodes) {
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
        SchemaPath expectedPath = TestUtils.createPath(true, expectedModule, "grouping-V", "container-grouping-V");
        assertEquals(expectedPath, containerGroupingV.getPath());
        childNodes = containerGroupingV.getChildNodes();
        assertEquals(2, childNodes.size());
        for (final DataSchemaNode childNode : childNodes) {
            TestUtils.checkIsAddedByUses(childNode, true);
        }

        // grouping-V/container-grouping-V/leaf-grouping-X
        final LeafSchemaNode leafXinContainerV = (LeafSchemaNode) containerGroupingV.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-X"));
        assertNotNull(leafXinContainerV);
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-V", "container-grouping-V",
            "leaf-grouping-X");
        assertEquals(expectedPath, leafXinContainerV.getPath());
        // grouping-V/container-grouping-V/leaf-grouping-Y
        final LeafSchemaNode leafYinContainerV = (LeafSchemaNode) containerGroupingV.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-Y"));
        assertNotNull(leafYinContainerV);
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-V", "container-grouping-V",
            "leaf-grouping-Y");
        assertEquals(expectedPath, leafYinContainerV.getPath());

        // grouping-X
        childNodes = gx.getChildNodes();
        assertEquals(2, childNodes.size());

        // grouping-X/leaf-grouping-X
        final LeafSchemaNode leafXinGX = (LeafSchemaNode) gx.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-X"));
        assertNotNull(leafXinGX);
        assertFalse(leafXinGX.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-X", "leaf-grouping-X");
        assertEquals(expectedPath, leafXinGX.getPath());

        // grouping-X/leaf-grouping-Y
        final LeafSchemaNode leafYinGX = (LeafSchemaNode) gx.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-Y"));
        assertNotNull(leafYinGX);
        assertTrue(leafYinGX.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-X", "leaf-grouping-Y");
        assertEquals(expectedPath, leafYinGX.getPath());

        // grouping-Y
        childNodes = gy.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Y/leaf-grouping-Y
        final LeafSchemaNode leafYinGY = (LeafSchemaNode) gy.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-Y"));
        assertNotNull(leafYinGY);
        assertFalse(leafYinGY.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-Y", "leaf-grouping-Y");
        assertEquals(expectedPath, leafYinGY.getPath());

        // grouping-Z
        childNodes = gz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Z/leaf-grouping-Z
        final LeafSchemaNode leafZinGZ = (LeafSchemaNode) gz.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-Z"));
        assertNotNull(leafZinGZ);
        assertFalse(leafZinGZ.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-Z", "leaf-grouping-Z");
        assertEquals(expectedPath, leafZinGZ.getPath());

        // grouping-ZZ
        childNodes = gzz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-ZZ/leaf-grouping-ZZ
        final LeafSchemaNode leafZZinGZZ = (LeafSchemaNode) gzz.getDataChildByName(QName.create(
                testModule.getQNameModule(), "leaf-grouping-ZZ"));
        assertNotNull(leafZZinGZZ);
        assertFalse(leafZZinGZZ.isAddedByUses());
        expectedPath = TestUtils.createPath(true, expectedModule, "grouping-ZZ", "leaf-grouping-ZZ");
        assertEquals(expectedPath, leafZZinGZZ.getPath());

        // TEST getOriginal from grouping-U
        assertEquals(
                gv.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-V")),
                SchemaNodeUtils.getRootOriginalIfPossible(gu.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-V"))));
        containerGroupingV = (ContainerSchemaNode) gu.getDataChildByName(QName.create(testModule.getQNameModule(),
                "container-grouping-V"));
        assertEquals(gv.getDataChildByName(QName.create(testModule.getQNameModule(), "container-grouping-V")),
                SchemaNodeUtils.getRootOriginalIfPossible(containerGroupingV));
        assertEquals(
                gx.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-X")),
                SchemaNodeUtils.getRootOriginalIfPossible(containerGroupingV.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-X"))));
        assertEquals(
                gy.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-Y")),
                SchemaNodeUtils.getRootOriginalIfPossible(containerGroupingV.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-Y"))));

        assertEquals(
                gz.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-Z")),
                SchemaNodeUtils.getRootOriginalIfPossible(gu.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-Z"))));
        assertEquals(
                gzz.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-ZZ")),
                SchemaNodeUtils.getRootOriginalIfPossible(gu.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-ZZ"))));

        // TEST getOriginal from grouping-V
        assertEquals(
                gz.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-Z")),
                SchemaNodeUtils.getRootOriginalIfPossible(gv.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-Z"))));
        assertEquals(
                gzz.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-ZZ")),
                SchemaNodeUtils.getRootOriginalIfPossible(gv.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-ZZ"))));

        // TEST getOriginal from grouping-X
        assertEquals(
                gy.getDataChildByName(QName.create(testModule.getQNameModule(), "leaf-grouping-Y")),
                SchemaNodeUtils.getRootOriginalIfPossible(gx.getDataChildByName(QName.create(
                        testModule.getQNameModule(), "leaf-grouping-Y"))));
    }

    @Test
    public void testAddedByUsesLeafTypeQName() throws Exception {
        final SchemaContext loadModules = TestUtils.loadModules(getClass().getResource("/added-by-uses-leaf-test")
                .toURI());
        assertEquals(2, loadModules.getModules().size());
        foo = TestUtils.findModule(loadModules, "foo").get();
        final Module imp = TestUtils.findModule(loadModules, "import-module").get();

        final LeafSchemaNode leaf = (LeafSchemaNode) ((ContainerSchemaNode) foo.getDataChildByName(QName.create(
                foo.getQNameModule(), "my-container")))
                .getDataChildByName(QName.create(foo.getQNameModule(), "my-leaf"));

        TypeDefinition<?> impType = null;
        final Set<TypeDefinition<?>> typeDefinitions = imp.getTypeDefinitions();
        for (final TypeDefinition<?> typeDefinition : typeDefinitions) {
            if (typeDefinition.getQName().getLocalName().equals("imp-type")) {
                impType = typeDefinition;
                break;
            }
        }

        assertNotNull(impType);
        assertEquals(leaf.getType().getQName(), impType.getQName());
    }
}
