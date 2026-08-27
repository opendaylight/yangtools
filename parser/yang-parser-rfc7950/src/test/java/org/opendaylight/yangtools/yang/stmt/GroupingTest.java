/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

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
import org.opendaylight.yangtools.yang.model.api.meta.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

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
        assertEquals(1, usesNode.asEffectiveStatement()
            .findFirstEffectiveSubstatement(RefineEffectiveStatement.class).orElseThrow().requireDeclared()
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

        assertEquals(MinElementsArgument.of(2), refineList.elementCountMatcher());

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
        // suffix U = added by uses
        // suffix G = defined in grouping

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
        final var dataU = assertInstanceOf(AnyxmlSchemaNode.class, destination.dataChildByName(fooQName("data")));
        assertTrue(dataU.isAddedByUses());

        final var dataG = assertInstanceOf(AnyxmlSchemaNode.class, grouping.dataChildByName(bazQName("data")));
        assertFalse(dataG.isAddedByUses());
        assertNotEquals(dataU, dataG);

        final var howU = assertInstanceOf(ChoiceSchemaNode.class, destination.dataChildByName(fooQName("how")));
        assertIsAddedByUses(howU, true);
        assertEquals(2, howU.getCases().size());

        final var howG = assertInstanceOf(ChoiceSchemaNode.class, grouping.dataChildByName(bazQName("how")));
        assertIsAddedByUses(howG, false);
        assertEquals(2, howG.getCases().size());
        assertNotEquals(howU, howG);

        final var addressU = assertInstanceOf(LeafSchemaNode.class, destination.dataChildByName(fooQName("address")));
        assertEquals(Optional.of("1.2.3.4"), addressU.typeDefinition().getDefaultValue());
        assertEquals(Optional.of("IP address of target node"), addressU.getDescription());
        assertEquals(Optional.of("address reference added by refine"), addressU.getReference());
        assertEquals(Optional.of(Boolean.FALSE), addressU.effectiveConfig());
        assertTrue(addressU.isAddedByUses());
        assertFalse(addressU.isMandatory());

        final var addressG = assertInstanceOf(LeafSchemaNode.class, grouping.dataChildByName(bazQName("address")));
        assertFalse(addressG.isAddedByUses());
        assertEquals(Optional.empty(), addressG.typeDefinition().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), addressG.getDescription());
        assertEquals(Optional.empty(), addressG.getReference());
        assertEquals(Optional.empty(), addressG.effectiveConfig());
        assertTrue(addressG.isMandatory());
        assertNotEquals(addressU, addressG);

        final var portU = assertInstanceOf(ContainerSchemaNode.class, destination.dataChildByName(fooQName("port")));
        assertIsAddedByUses(portU, true);

        final var portG = assertInstanceOf(ContainerSchemaNode.class, grouping.dataChildByName(bazQName("port")));
        assertIsAddedByUses(portG, false);
        assertNotEquals(portU, portG);

        final var addressesU = assertInstanceOf(ListSchemaNode.class,
            destination.dataChildByName(fooQName("addresses")));
        assertIsAddedByUses(addressesU, true);

        final var addressesG = assertInstanceOf(ListSchemaNode.class, grouping.dataChildByName(bazQName("addresses")));
        assertIsAddedByUses(addressesG, false);
        assertNotEquals(addressesU, addressesG);

        // grouping defined by 'uses'
        final var groupingsU = destination.getGroupings();
        assertEquals(0, groupingsU.size());

        // grouping defined in 'grouping' node
        final var groupingsG = grouping.getGroupings();
        assertEquals(1, groupingsG.size());
        final var groupingG = groupingsG.iterator().next();
        assertIsAddedByUses(groupingG, false);

        assertEquals(1, grouping.asEffectiveStatement().requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class).size());
    }

    @Test
    void testUsesUnderModule() {
        // suffix U = added by uses
        // suffix G = defined in grouping

        // get grouping
        final var groupings = BAZ.getGroupings();
        assertEquals(1, groupings.size());
        final var grouping = groupings.iterator().next();

        // check uses
        final var uses = FOO.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final var dataU = assertInstanceOf(AnyxmlSchemaNode.class, FOO.dataChildByName(fooQName("data")));
        assertTrue(dataU.isAddedByUses());

        final var dataG = assertInstanceOf(AnyxmlSchemaNode.class, grouping.dataChildByName(bazQName("data")));
        assertFalse(dataG.isAddedByUses());
        assertNotEquals(dataU, dataG);

        final var howU = assertInstanceOf(ChoiceSchemaNode.class, FOO.dataChildByName(fooQName("how")));
        assertIsAddedByUses(howU, true);
        assertFalse(howU.isAugmenting());
        final var casesU = howU.getCases();
        assertEquals(2, casesU.size());
        final var interval = howU.findCaseNodes("interval").iterator().next();
        assertFalse(interval.isAugmenting());
        final var name = assertInstanceOf(LeafSchemaNode.class, interval.dataChildByName(fooQName("name")));
        assertTrue(name.isAugmenting());
        final var intervalLeaf = assertInstanceOf(LeafSchemaNode.class, interval.dataChildByName(fooQName("interval")));
        assertFalse(intervalLeaf.isAugmenting());

        final var howG = assertInstanceOf(ChoiceSchemaNode.class, grouping.dataChildByName(bazQName("how")));
        assertIsAddedByUses(howG, false);
        assertNotEquals(howU, howG);

        final var addressU = assertInstanceOf(LeafSchemaNode.class, FOO.dataChildByName(fooQName("address")));
        assertEquals(Optional.empty(), addressU.typeDefinition().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), addressU.getDescription());
        assertFalse(addressU.getReference().isPresent());
        assertEquals(Optional.empty(), addressU.effectiveConfig());
        assertTrue(addressU.isAddedByUses());

        final var addressG = assertInstanceOf(LeafSchemaNode.class, grouping.dataChildByName(bazQName("address")));
        assertFalse(addressG.isAddedByUses());
        assertEquals(Optional.empty(), addressG.typeDefinition().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), addressG.getDescription());
        assertFalse(addressG.getReference().isPresent());
        assertEquals(Optional.empty(), addressG.effectiveConfig());
        assertNotEquals(addressU, addressG);

        final var portU = assertInstanceOf(ContainerSchemaNode.class, FOO.dataChildByName(fooQName("port")));
        assertIsAddedByUses(portU, true);

        final var portG = assertInstanceOf(ContainerSchemaNode.class, grouping.dataChildByName(bazQName("port")));
        assertIsAddedByUses(portG, false);
        assertNotEquals(portU, portG);

        final var addressesU = assertInstanceOf(ListSchemaNode.class, FOO.dataChildByName(fooQName("addresses")));
        assertIsAddedByUses(addressesU, true);

        final var addressesG = assertInstanceOf(ListSchemaNode.class, grouping.dataChildByName(bazQName("addresses")));
        assertIsAddedByUses(addressesG, false);
        assertNotEquals(addressesU, addressesG);

        // grouping defined by 'uses'
        final var groupingsU = FOO.getGroupings();
        assertEquals(0, groupingsU.size());

        // grouping defined in 'grouping' node
        final var groupingsG = grouping.getGroupings();
        assertEquals(1, groupingsG.size());
        final var groupingG = groupingsG.iterator().next();
        assertIsAddedByUses(groupingG, false);

        assertEquals(1, grouping.asEffectiveStatement().requireDeclared()
            .declaredSubstatements(UnrecognizedStatement.class).size());

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
            switch (gd.getQName().getLocalName()) {
                case "grouping-U" -> gu = gd;
                case "grouping-V" -> gv = gd;
                case "grouping-X" -> gx = gd;
                case "grouping-Y" -> gy = gd;
                case "grouping-Z" -> gz = gd;
                case "grouping-ZZ" -> gzz = gd;
                default -> {
                    // No-op
                }
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
            switch (childNode.getQName().getLocalName()) {
                case "leaf-grouping-V" ->
                    leafGroupingV = assertInstanceOf(LeafSchemaNode.class, childNode);
                case "container-grouping-V" ->
                    containerGroupingV = assertInstanceOf(ContainerSchemaNode.class, childNode);
                default -> assertIsAddedByUses(childNode, true);
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
    void testAddedByUsesLeafTypeQName() {
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
        assertEquals(leaf.typeDefinition().getQName(), impType.getQName());
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
