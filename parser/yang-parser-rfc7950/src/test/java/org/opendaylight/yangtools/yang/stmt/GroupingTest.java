/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

class GroupingTest extends AbstractModelTest {
    @Test
    void testRefine() {
        final var peer = assertInstanceOf(ContainerSchemaNode.class, FOO.dataChildByName(fooQName("peer")));
        final var destination = assertInstanceOf(ContainerSchemaNode.class,
            peer.dataChildByName(fooQName("destination")));

        final var usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        final var usesNode = usesNodes.iterator().next();
        final var refines = usesNode.getRefines();
        assertEquals(4, refines.size());

        assertEquals(List.of(
            Descendant.of(fooQName("address")),
            Descendant.of(fooQName("port")),
            Descendant.of(fooQName("addresses")),
            Descendant.of(fooQName("addresses"), fooQName("id"))),
            List.copyOf(usesNode.getRefines()));

        // leaf address
        final var refineLeaf = assertInstanceOf(LeafSchemaNode.class, destination.dataChildByName(fooQName("address")));
        assertEquals(Optional.of("IP address of target node"), refineLeaf.getDescription());
        assertEquals(Optional.of("address reference added by refine"), refineLeaf.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineLeaf.effectiveConfig());
        assertFalse(refineLeaf.isMandatory());
        final var leafMustConstraints = refineLeaf.getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        final var leafMust = leafMustConstraints.iterator().next();
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.getXpath().toString());
        assertEquals(0, refineLeaf.getUnknownSchemaNodes().size());
        assertEquals(1, usesNode.asEffectiveStatement()
            .findFirstEffectiveSubstatement(RefineEffectiveStatement.class).orElseThrow().getDeclared()
            .declaredSubstatements(UnrecognizedStatement.class).size());

        // container port
        final var refineContainer = assertInstanceOf(ContainerSchemaNode.class,
            destination.dataChildByName(fooQName("port")));

        final var mustConstraints = refineContainer.getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals(Optional.of("description of port defined by refine"), refineContainer.getDescription());
        assertEquals(Optional.of("port reference added by refine"), refineContainer.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineContainer.effectiveConfig());
        assertTrue(refineContainer.isPresenceContainer());

//      // list addresses
        final var refineList = assertInstanceOf(ListSchemaNode.class,
            destination.dataChildByName(fooQName("addresses")));
        assertEquals(Optional.of("description of addresses defined by refine"), refineList.getDescription());
        assertEquals(Optional.of("addresses reference added by refine"), refineList.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineList.effectiveConfig());

        final var constraint = refineList.getElementCountConstraint().orElseThrow();
        assertEquals(2, constraint.getMinElements());
        assertNull(constraint.getMaxElements());

        // leaf id
        final var refineInnerLeaf = assertInstanceOf(LeafSchemaNode.class, refineList.dataChildByName(fooQName("id")));
        assertEquals(Optional.of("id of address"), refineInnerLeaf.getDescription());
    }

    @Test
    void testGrouping() {
        final var groupings = BAZ.getGroupings();
        assertEquals(1, groupings.size());
        final var grouping = groupings.iterator().next();
        final var children = grouping.getChildNodes();
        assertEquals(5, children.size());
    }

    @Test
    void testUses() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping

        // get grouping
        final var groupings = BAZ.getGroupings();
        assertEquals(1, groupings.size());
        final var grouping = groupings.iterator().next();

        // get node containing uses
        final var peer = assertInstanceOf(ContainerSchemaNode.class, FOO.dataChildByName(fooQName("peer")));
        final var destination = assertInstanceOf(ContainerSchemaNode.class,
            peer.dataChildByName(fooQName("destination")));

        // check uses
        final var uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final var data_u = assertInstanceOf(AnyxmlSchemaNode.class, destination.dataChildByName(fooQName("data")));
        assertTrue(data_u.isAddedByUses());

        final var data_g = assertInstanceOf(AnyxmlSchemaNode.class, grouping.dataChildByName(bazQName("data")));
        assertFalse(data_g.isAddedByUses());
        assertNotEquals(data_u, data_g);

        final var how_u = assertInstanceOf(ChoiceSchemaNode.class, destination.dataChildByName(fooQName("how")));
        assertIsAddedByUses(how_u, true);
        assertEquals(2, how_u.getCases().size());

        final var how_g = assertInstanceOf(ChoiceSchemaNode.class, grouping.dataChildByName(bazQName("how")));
        assertIsAddedByUses(how_g, false);
        assertEquals(2, how_g.getCases().size());
        assertNotEquals(how_u, how_g);

        final var address_u = assertInstanceOf(LeafSchemaNode.class, destination.dataChildByName(fooQName("address")));
        assertEquals(Optional.of("1.2.3.4"), address_u.getType().getDefaultValue());
        assertEquals(Optional.of("IP address of target node"), address_u.getDescription());
        assertEquals(Optional.of("address reference added by refine"), address_u.getReference());
        assertEquals(Optional.of(Boolean.FALSE), address_u.effectiveConfig());
        assertTrue(address_u.isAddedByUses());
        assertFalse(address_u.isMandatory());

        final var address_g = assertInstanceOf(LeafSchemaNode.class, grouping.dataChildByName(bazQName("address")));
        assertFalse(address_g.isAddedByUses());
        assertEquals(Optional.empty(), address_g.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_g.getDescription());
        assertEquals(Optional.empty(), address_g.getReference());
        assertEquals(Optional.empty(), address_g.effectiveConfig());
        assertTrue(address_g.isMandatory());
        assertNotEquals(address_u, address_g);

        final var port_u = assertInstanceOf(ContainerSchemaNode.class, destination.dataChildByName(fooQName("port")));
        assertIsAddedByUses(port_u, true);

        final var port_g = assertInstanceOf(ContainerSchemaNode.class, grouping.dataChildByName(bazQName("port")));
        assertIsAddedByUses(port_g, false);
        assertNotEquals(port_u, port_g);

        final var addresses_u = assertInstanceOf(ListSchemaNode.class,
            destination.dataChildByName(fooQName("addresses")));
        assertIsAddedByUses(addresses_u, true);

        final var addresses_g = assertInstanceOf(ListSchemaNode.class, grouping.dataChildByName(bazQName("addresses")));
        assertIsAddedByUses(addresses_g, false);
        assertNotEquals(addresses_u, addresses_g);

        // grouping defined by 'uses'
        final var groupings_u = destination.getGroupings();
        assertEquals(0, groupings_u.size());

        // grouping defined in 'grouping' node
        final var groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final var grouping_g = groupings_g.iterator().next();
        assertIsAddedByUses(grouping_g, false);

        assertEquals(0, destination.getUnknownSchemaNodes().size());
        assertEquals(1,
            grouping.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class).size());
    }

    @Test
    void testUsesUnderModule() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping

        // get grouping
        final var groupings = BAZ.getGroupings();
        assertEquals(1, groupings.size());
        final var grouping = groupings.iterator().next();

        // check uses
        final var uses = FOO.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final var data_u = assertInstanceOf(AnyxmlSchemaNode.class, FOO.dataChildByName(fooQName("data")));
        assertTrue(data_u.isAddedByUses());

        final var data_g = assertInstanceOf(AnyxmlSchemaNode.class, grouping.dataChildByName(bazQName("data")));
        assertFalse(data_g.isAddedByUses());
        assertNotEquals(data_u, data_g);

        final var how_u = assertInstanceOf(ChoiceSchemaNode.class, FOO.dataChildByName(fooQName("how")));
        assertIsAddedByUses(how_u, true);
        assertFalse(how_u.isAugmenting());
        final var cases_u = how_u.getCases();
        assertEquals(2, cases_u.size());
        final var interval = how_u.findCaseNodes("interval").iterator().next();
        assertFalse(interval.isAugmenting());
        final var name = assertInstanceOf(LeafSchemaNode.class, interval.dataChildByName(fooQName("name")));
        assertTrue(name.isAugmenting());
        final var intervalLeaf = assertInstanceOf(LeafSchemaNode.class, interval.dataChildByName(fooQName("interval")));
        assertFalse(intervalLeaf.isAugmenting());

        final var how_g = assertInstanceOf(ChoiceSchemaNode.class, grouping.dataChildByName(bazQName("how")));
        assertIsAddedByUses(how_g, false);
        assertNotEquals(how_u, how_g);

        final var address_u = assertInstanceOf(LeafSchemaNode.class, FOO.dataChildByName(fooQName("address")));
        assertEquals(Optional.empty(), address_u.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_u.getDescription());
        assertFalse(address_u.getReference().isPresent());
        assertEquals(Optional.empty(), address_u.effectiveConfig());
        assertTrue(address_u.isAddedByUses());

        final var address_g = assertInstanceOf(LeafSchemaNode.class, grouping.dataChildByName(bazQName("address")));
        assertFalse(address_g.isAddedByUses());
        assertEquals(Optional.empty(), address_g.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_g.getDescription());
        assertFalse(address_g.getReference().isPresent());
        assertEquals(Optional.empty(), address_g.effectiveConfig());
        assertNotEquals(address_u, address_g);

        final var port_u = assertInstanceOf(ContainerSchemaNode.class, FOO.dataChildByName(fooQName("port")));
        assertIsAddedByUses(port_u, true);

        final var port_g = assertInstanceOf(ContainerSchemaNode.class, grouping.dataChildByName(bazQName("port")));
        assertIsAddedByUses(port_g, false);
        assertNotEquals(port_u, port_g);

        final var addresses_u = assertInstanceOf(ListSchemaNode.class, FOO.dataChildByName(fooQName("addresses")));
        assertIsAddedByUses(addresses_u, true);

        final var addresses_g = assertInstanceOf(ListSchemaNode.class, grouping.dataChildByName(bazQName("addresses")));
        assertIsAddedByUses(addresses_g, false);
        assertNotEquals(addresses_u, addresses_g);

        // grouping defined by 'uses'
        final var groupings_u = FOO.getGroupings();
        assertEquals(0, groupings_u.size());

        // grouping defined in 'grouping' node
        final var groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final var grouping_g = groupings_g.iterator().next();
        assertIsAddedByUses(grouping_g, false);

        assertEquals(0, grouping.getUnknownSchemaNodes().size());
        assertEquals(1, grouping.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class)
            .size());

        final var un = uses.iterator().next();
        final var usesAugments = un.getAugmentations();
        assertEquals(1, usesAugments.size());
        final var augment = usesAugments.iterator().next();
        assertEquals(Optional.of("inner augment"), augment.getDescription());
        final var children = augment.getChildNodes();
        assertEquals(1, children.size());
        final var leaf = assertInstanceOf(LeafSchemaNode.class, children.iterator().next());
        assertEquals("name", leaf.getQName().getLocalName());
    }

    @Test
    void testCascadeUses() throws Exception {
        final var loadModules = TestUtils.parseYangSource("/grouping-test/cascade-uses.yang");
        assertEquals(1, loadModules.getModules().size());

        final var testModule =  Iterables.getOnlyElement(loadModules.findModules("cascade-uses"));
        final var namespace = testModule.getQNameModule();
        final var groupings = testModule.getGroupings();

        GroupingDefinition gu = null;
        GroupingDefinition gv = null;
        GroupingDefinition gx = null;
        GroupingDefinition gy = null;
        GroupingDefinition gz = null;
        GroupingDefinition gzz = null;
        for (var gd : groupings) {
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

        final var expectedModule = QNameModule.of("urn:grouping:cascade-uses", "2013-07-18");

        // grouping-U
        var childNodes = gu.getChildNodes();
        assertEquals(7, childNodes.size());

        final var leafGroupingU = assertInstanceOf(LeafSchemaNode.class, gu.dataChildByName(
            QName.create(namespace, "leaf-grouping-U")));
        assertFalse(leafGroupingU.isAddedByUses());

        for (var childNode : childNodes) {
            if (!childNode.getQName().equals(leafGroupingU.getQName())) {
                assertIsAddedByUses(childNode, true);
            }
        }

        // grouping-V
        childNodes = gv.getChildNodes();
        assertEquals(4, childNodes.size());
        LeafSchemaNode leafGroupingV = null;
        ContainerSchemaNode containerGroupingV = null;
        for (var childNode : childNodes) {
            if ("leaf-grouping-V".equals(childNode.getQName().getLocalName())) {
                leafGroupingV = assertInstanceOf(LeafSchemaNode.class, childNode);
            } else if ("container-grouping-V".equals(childNode.getQName().getLocalName())) {
                containerGroupingV = assertInstanceOf(ContainerSchemaNode.class, childNode);
            } else {
                assertIsAddedByUses(childNode, true);
            }
        }
        assertNotNull(leafGroupingV);
        assertFalse(leafGroupingV.isAddedByUses());

        // grouping-V/container-grouping-V
        assertNotNull(containerGroupingV);
        assertFalse(containerGroupingV.isAddedByUses());
        assertEquals(QName.create(expectedModule, "container-grouping-V"), containerGroupingV.getQName());
        childNodes = containerGroupingV.getChildNodes();
        assertEquals(2, childNodes.size());
        for (var childNode : childNodes) {
            assertIsAddedByUses(childNode, true);
        }

        // grouping-V/container-grouping-V/leaf-grouping-X
        final var leafXinContainerV = assertInstanceOf(LeafSchemaNode.class, containerGroupingV.dataChildByName(
            QName.create(namespace, "leaf-grouping-X")));
        assertEquals(QName.create(expectedModule, "leaf-grouping-X"), leafXinContainerV.getQName());
        // grouping-V/container-grouping-V/leaf-grouping-Y
        final var leafYinContainerV = assertInstanceOf(LeafSchemaNode.class, containerGroupingV.dataChildByName(
            QName.create(namespace, "leaf-grouping-Y")));
        assertEquals(QName.create(expectedModule, "leaf-grouping-Y"), leafYinContainerV.getQName());

        // grouping-X
        childNodes = gx.getChildNodes();
        assertEquals(2, childNodes.size());

        // grouping-X/leaf-grouping-X
        final var leafXinGX = assertInstanceOf(LeafSchemaNode.class, gx.dataChildByName(
            QName.create(namespace, "leaf-grouping-X")));
        assertFalse(leafXinGX.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-X"), leafXinGX.getQName());

        // grouping-X/leaf-grouping-Y
        final var leafYinGX = assertInstanceOf(LeafSchemaNode.class, gx.dataChildByName(
            QName.create(namespace, "leaf-grouping-Y")));
        assertTrue(leafYinGX.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-Y"), leafYinGX.getQName());

        // grouping-Y
        childNodes = gy.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Y/leaf-grouping-Y
        final var leafYinGY = assertInstanceOf(LeafSchemaNode.class, gy.dataChildByName(
            QName.create(namespace, "leaf-grouping-Y")));
        assertFalse(leafYinGY.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-Y"), leafYinGY.getQName());

        // grouping-Z
        childNodes = gz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Z/leaf-grouping-Z
        final var leafZinGZ = assertInstanceOf(LeafSchemaNode.class, gz.dataChildByName(
            QName.create(namespace, "leaf-grouping-Z")));
        assertFalse(leafZinGZ.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-Z"), leafZinGZ.getQName());

        // grouping-ZZ
        childNodes = gzz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-ZZ/leaf-grouping-ZZ
        final var leafZZinGZZ = assertInstanceOf(LeafSchemaNode.class, gzz.dataChildByName(
            QName.create(namespace, "leaf-grouping-ZZ")));
        assertFalse(leafZZinGZZ.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-ZZ"), leafZZinGZZ.getQName());
    }

    @Test
    void testAddedByUsesLeafTypeQName() throws Exception {
        final var loadModules = assertEffectiveModelDir("/added-by-uses-leaf-test");
        assertEquals(2, loadModules.getModules().size());

        final var foo = Iterables.getOnlyElement(loadModules.findModules("foo"));
        final var imp = Iterables.getOnlyElement(loadModules.findModules("import-module"));

        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            assertInstanceOf(ContainerSchemaNode.class, foo.dataChildByName(
                QName.create(foo.getQNameModule(), "my-container")))
            .dataChildByName(QName.create(foo.getQNameModule(), "my-leaf")));

        TypeDefinition<?> impType = null;
        for (var typeDefinition : imp.getTypeDefinitions()) {
            if (typeDefinition.getQName().getLocalName().equals("imp-type")) {
                impType = typeDefinition;
                break;
            }
        }

        assertNotNull(impType);
        assertEquals(leaf.getType().getQName(), impType.getQName());
    }

    private static void assertIsAddedByUses(final GroupingDefinition node, final boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        for (var child : node.getChildNodes()) {
            assertIsAddedByUses(child, expected);
        }
    }

    /**
     * Check if node has addedByUses flag set to expected value. In case this is
     * DataNodeContainer/ChoiceNode, check its child nodes/case nodes too.
     *
     * @param node node to check
     * @param expected expected value
     */
    private static void assertIsAddedByUses(final DataSchemaNode node, final boolean expected) {
        assertEquals(expected, node.isAddedByUses());
        if (node instanceof DataNodeContainer container) {
            for (var child : container.getChildNodes()) {
                assertIsAddedByUses(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode choice) {
            for (var caseNode : choice.getCases()) {
                assertIsAddedByUses(caseNode, expected);
            }
        }
    }
}
