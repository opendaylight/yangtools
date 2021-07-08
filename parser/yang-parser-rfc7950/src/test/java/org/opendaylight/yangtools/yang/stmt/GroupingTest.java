/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.RefineEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

public class GroupingTest {
    private static EffectiveModelContext ctx;
    private static Module foo;
    private static Module baz;

    @BeforeClass
    public static void beforeClass() throws Exception {
        ctx = TestUtils.parseYangSources("/model");
        assertEquals(3, ctx.getModules().size());

        foo = Iterables.getOnlyElement(ctx.findModules("foo"));
        baz = Iterables.getOnlyElement(ctx.findModules("baz"));
    }

    @Test
    public void testRefine() {
        final ContainerSchemaNode peer = (ContainerSchemaNode) foo.getDataChildByName(QName.create(
                foo.getQNameModule(), "peer"));
        final ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                foo.getQNameModule(), "destination"));

        final Collection<? extends UsesNode> usesNodes = destination.getUses();
        assertEquals(1, usesNodes.size());
        final UsesNode usesNode = usesNodes.iterator().next();
        final Map<Descendant, SchemaNode> refines = usesNode.getRefines();
        assertEquals(4, refines.size());

        LeafSchemaNode refineLeaf = null;
        ContainerSchemaNode refineContainer = null;
        ListSchemaNode refineList = null;
        LeafSchemaNode refineInnerLeaf = null;
        for (final Map.Entry<Descendant, SchemaNode> entry : refines.entrySet()) {
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
        for (final Map.Entry<Descendant, SchemaNode> entry : refines.entrySet()) {
            final SchemaNode value = entry.getValue();
            if ("id".equals(value.getQName().getLocalName())) {
                refineInnerLeaf = (LeafSchemaNode) refineList.getDataChildByName(value.getQName());
            }
        }

        // leaf address
        assertNotNull(refineLeaf);
        assertEquals(Optional.of("IP address of target node"), refineLeaf.getDescription());
        assertEquals(Optional.of("address reference added by refine"), refineLeaf.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineLeaf.effectiveConfig());
        assertFalse(refineLeaf.isMandatory());
        final Collection<? extends MustDefinition> leafMustConstraints = refineLeaf.getMustConstraints();
        assertEquals(1, leafMustConstraints.size());
        final MustDefinition leafMust = leafMustConstraints.iterator().next();
        assertEquals("ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)", leafMust.getXpath().toString());
        assertEquals(0, refineLeaf.getUnknownSchemaNodes().size());
        assertEquals(1, usesNode.asEffectiveStatement()
            .findFirstEffectiveSubstatement(RefineEffectiveStatement.class).orElseThrow().getDeclared()
            .declaredSubstatements(UnrecognizedStatement.class).size());

        // container port
        assertNotNull(refineContainer);
        final Collection<? extends MustDefinition> mustConstraints = refineContainer.getMustConstraints();
        assertTrue(mustConstraints.isEmpty());
        assertEquals(Optional.of("description of port defined by refine"), refineContainer.getDescription());
        assertEquals(Optional.of("port reference added by refine"), refineContainer.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineContainer.effectiveConfig());
        assertTrue(refineContainer.isPresenceContainer());

        // list addresses
        assertEquals(Optional.of("description of addresses defined by refine"), refineList.getDescription());
        assertEquals(Optional.of("addresses reference added by refine"), refineList.getReference());
        assertEquals(Optional.of(Boolean.FALSE), refineList.effectiveConfig());

        final ElementCountConstraint constraint = refineList.getElementCountConstraint().get();
        assertEquals((Object) 2, constraint.getMinElements());
        assertNull(constraint.getMaxElements());

        // leaf id
        assertNotNull(refineInnerLeaf);
        assertEquals(Optional.of("id of address"), refineInnerLeaf.getDescription());
    }

    @Test
    public void testGrouping() {
        final Collection<? extends GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();
        final Collection<? extends DataSchemaNode> children = grouping.getChildNodes();
        assertEquals(5, children.size());
    }

    @Test
    public void testUses() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping


        // get grouping
        final Collection<? extends GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();

        // get node containing uses
        final ContainerSchemaNode peer = (ContainerSchemaNode) foo.getDataChildByName(QName.create(
                foo.getQNameModule(), "peer"));
        final ContainerSchemaNode destination = (ContainerSchemaNode) peer.getDataChildByName(QName.create(
                foo.getQNameModule(), "destination"));

        // check uses
        final Collection<? extends UsesNode> uses = destination.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final AnyxmlSchemaNode data_u = (AnyxmlSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "data"));
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        final AnyxmlSchemaNode data_g = (AnyxmlSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "data"));
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));
        assertEquals(data_g, extractOriginal(data_u));

        final ChoiceSchemaNode how_u = (ChoiceSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "how"));
        assertNotNull(how_u);
        assertIsAddedByUses(how_u, true);
        assertEquals(2, how_u.getCases().size());

        final ChoiceSchemaNode how_g = (ChoiceSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "how"));
        assertNotNull(how_g);
        assertIsAddedByUses(how_g, false);
        assertEquals(2, how_g.getCases().size());
        assertFalse(how_u.equals(how_g));
        assertEquals(how_g, extractOriginal(how_u));

        final LeafSchemaNode address_u = (LeafSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "address"));
        assertNotNull(address_u);
        assertEquals(Optional.of("1.2.3.4"), address_u.getType().getDefaultValue());
        assertEquals(Optional.of("IP address of target node"), address_u.getDescription());
        assertEquals(Optional.of("address reference added by refine"), address_u.getReference());
        assertEquals(Optional.of(Boolean.FALSE), address_u.effectiveConfig());
        assertTrue(address_u.isAddedByUses());
        assertFalse(address_u.isMandatory());

        final LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "address"));
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertEquals(Optional.empty(), address_g.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_g.getDescription());
        assertFalse(address_g.getReference().isPresent());
        assertEquals(Optional.empty(), address_g.effectiveConfig());
        assertFalse(address_u.equals(address_g));
        assertTrue(address_g.isMandatory());
        assertEquals(address_g, extractOriginal(address_u));

        final ContainerSchemaNode port_u = (ContainerSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "port"));
        assertNotNull(port_u);
        assertIsAddedByUses(port_u, true);

        final ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "port"));
        assertNotNull(port_g);
        assertIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));
        assertEquals(port_g, extractOriginal(port_u));

        final ListSchemaNode addresses_u = (ListSchemaNode) destination.getDataChildByName(QName.create(
                foo.getQNameModule(), "addresses"));
        assertNotNull(addresses_u);
        assertIsAddedByUses(addresses_u, true);

        final ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "addresses"));
        assertNotNull(addresses_g);
        assertIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));
        assertEquals(addresses_g, extractOriginal(addresses_u));

        // grouping defined by 'uses'
        final Collection<? extends GroupingDefinition> groupings_u = destination.getGroupings();
        assertEquals(0, groupings_u.size());

        // grouping defined in 'grouping' node
        final Collection<? extends GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final GroupingDefinition grouping_g = groupings_g.iterator().next();
        assertIsAddedByUses(grouping_g, false);

        assertEquals(0, destination.getUnknownSchemaNodes().size());
        assertEquals(1,
            grouping.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class).size());
    }

    @Test
    public void testUsesUnderModule() {
        // suffix _u = added by uses
        // suffix _g = defined in grouping

        // get grouping
        final Collection<? extends GroupingDefinition> groupings = baz.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition grouping = groupings.iterator().next();

        // check uses
        final Collection<? extends UsesNode> uses = foo.getUses();
        assertEquals(1, uses.size());

        // check uses process
        final AnyxmlSchemaNode data_u = (AnyxmlSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "data"));
        assertNotNull(data_u);
        assertTrue(data_u.isAddedByUses());

        final AnyxmlSchemaNode data_g = (AnyxmlSchemaNode) grouping.getDataChildByName(QName.create(
            baz.getQNameModule(), "data"));
        assertNotNull(data_g);
        assertFalse(data_g.isAddedByUses());
        assertFalse(data_u.equals(data_g));
        assertEquals(data_g, extractOriginal(data_u));

        final ChoiceSchemaNode how_u = (ChoiceSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "how"));
        assertNotNull(how_u);
        assertIsAddedByUses(how_u, true);
        assertFalse(how_u.isAugmenting());
        final Collection<? extends CaseSchemaNode> cases_u = how_u.getCases();
        assertEquals(2, cases_u.size());
        final CaseSchemaNode interval = how_u.findCaseNodes("interval").iterator().next();
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
        assertIsAddedByUses(how_g, false);
        assertFalse(how_u.equals(how_g));
        assertEquals(how_g, extractOriginal(how_u));

        final LeafSchemaNode address_u = (LeafSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "address"));
        assertNotNull(address_u);
        assertEquals(Optional.empty(), address_u.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_u.getDescription());
        assertFalse(address_u.getReference().isPresent());
        assertEquals(Optional.empty(), address_u.effectiveConfig());
        assertTrue(address_u.isAddedByUses());

        final LeafSchemaNode address_g = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "address"));
        assertNotNull(address_g);
        assertFalse(address_g.isAddedByUses());
        assertEquals(Optional.empty(), address_g.getType().getDefaultValue());
        assertEquals(Optional.of("Target IP address"), address_g.getDescription());
        assertFalse(address_g.getReference().isPresent());
        assertEquals(Optional.empty(), address_g.effectiveConfig());
        assertFalse(address_u.equals(address_g));
        assertEquals(address_g, extractOriginal(address_u));

        final ContainerSchemaNode port_u = (ContainerSchemaNode) foo.getDataChildByName(QName.create(
                foo.getQNameModule(), "port"));
        assertNotNull(port_u);
        assertIsAddedByUses(port_u, true);

        final ContainerSchemaNode port_g = (ContainerSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "port"));
        assertNotNull(port_g);
        assertIsAddedByUses(port_g, false);
        assertFalse(port_u.equals(port_g));
        assertEquals(port_g, extractOriginal(port_u));

        final ListSchemaNode addresses_u = (ListSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(),
                "addresses"));
        assertNotNull(addresses_u);
        assertIsAddedByUses(addresses_u, true);

        final ListSchemaNode addresses_g = (ListSchemaNode) grouping.getDataChildByName(QName.create(
                baz.getQNameModule(), "addresses"));
        assertNotNull(addresses_g);
        assertIsAddedByUses(addresses_g, false);
        assertFalse(addresses_u.equals(addresses_g));
        assertEquals(addresses_g, extractOriginal(addresses_u));

        // grouping defined by 'uses'
        final Collection<? extends GroupingDefinition> groupings_u = foo.getGroupings();
        assertEquals(0, groupings_u.size());

        // grouping defined in 'grouping' node
        final Collection<? extends GroupingDefinition> groupings_g = grouping.getGroupings();
        assertEquals(1, groupings_g.size());
        final GroupingDefinition grouping_g = groupings_g.iterator().next();
        assertIsAddedByUses(grouping_g, false);

        assertEquals(0, grouping.getUnknownSchemaNodes().size());
        assertEquals(1, grouping.asEffectiveStatement().getDeclared().declaredSubstatements(UnrecognizedStatement.class)
            .size());

        final UsesNode un = uses.iterator().next();
        final Collection<? extends AugmentationSchemaNode> usesAugments = un.getAugmentations();
        assertEquals(1, usesAugments.size());
        final AugmentationSchemaNode augment = usesAugments.iterator().next();
        assertEquals(Optional.of("inner augment"), augment.getDescription());
        final Collection<? extends DataSchemaNode> children = augment.getChildNodes();
        assertEquals(1, children.size());
        final DataSchemaNode leaf = children.iterator().next();
        assertTrue(leaf instanceof LeafSchemaNode);
        assertEquals("name", leaf.getQName().getLocalName());
    }

    @Test
    public void testCascadeUses() throws Exception {
        ctx = TestUtils.parseYangSource("/grouping-test/cascade-uses.yang");
        assertEquals(1, ctx.getModules().size());

        final Module testModule = TestUtils.findModule(ctx, "cascade-uses").get();
        final QNameModule namespace = testModule.getQNameModule();
        final Collection<? extends GroupingDefinition> groupings = testModule.getGroupings();

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

        final QNameModule expectedModule = QNameModule.create(XMLNamespace.of("urn:grouping:cascade-uses"),
            Revision.of("2013-07-18"));

        // grouping-U
        Collection<? extends DataSchemaNode> childNodes = gu.getChildNodes();
        assertEquals(7, childNodes.size());

        final LeafSchemaNode leafGroupingU = (LeafSchemaNode) gu.getDataChildByName(
            QName.create(namespace, "leaf-grouping-U"));
        assertNotNull(leafGroupingU);
        assertFalse(leafGroupingU.isAddedByUses());
        assertNull(extractOriginal(leafGroupingU));

        for (final DataSchemaNode childNode : childNodes) {
            if (!childNode.getQName().equals(leafGroupingU.getQName())) {
                assertIsAddedByUses(childNode, true);
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
        for (final DataSchemaNode childNode : childNodes) {
            assertIsAddedByUses(childNode, true);
        }

        // grouping-V/container-grouping-V/leaf-grouping-X
        final LeafSchemaNode leafXinContainerV = (LeafSchemaNode) containerGroupingV.getDataChildByName(
            QName.create(namespace, "leaf-grouping-X"));
        assertEquals(QName.create(expectedModule, "leaf-grouping-X"), leafXinContainerV.getQName());
        // grouping-V/container-grouping-V/leaf-grouping-Y
        final LeafSchemaNode leafYinContainerV = (LeafSchemaNode) containerGroupingV.getDataChildByName(
            QName.create(namespace, "leaf-grouping-Y"));
        assertEquals(QName.create(expectedModule, "leaf-grouping-Y"), leafYinContainerV.getQName());

        // grouping-X
        childNodes = gx.getChildNodes();
        assertEquals(2, childNodes.size());

        // grouping-X/leaf-grouping-X
        final LeafSchemaNode leafXinGX = (LeafSchemaNode) gx.getDataChildByName(
            QName.create(namespace, "leaf-grouping-X"));
        assertFalse(leafXinGX.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-X"), leafXinGX.getQName());

        // grouping-X/leaf-grouping-Y
        final LeafSchemaNode leafYinGX = (LeafSchemaNode) gx.getDataChildByName(
            QName.create(namespace, "leaf-grouping-Y"));
        assertTrue(leafYinGX.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-Y"), leafYinGX.getQName());

        // grouping-Y
        childNodes = gy.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Y/leaf-grouping-Y
        final LeafSchemaNode leafYinGY = (LeafSchemaNode) gy.getDataChildByName(
            QName.create(namespace, "leaf-grouping-Y"));
        assertFalse(leafYinGY.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-Y"), leafYinGY);

        // grouping-Z
        childNodes = gz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-Z/leaf-grouping-Z
        final LeafSchemaNode leafZinGZ = (LeafSchemaNode) gz.getDataChildByName(
            QName.create(namespace, "leaf-grouping-Z"));
        assertFalse(leafZinGZ.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-Z"), leafZinGZ);

        // grouping-ZZ
        childNodes = gzz.getChildNodes();
        assertEquals(1, childNodes.size());

        // grouping-ZZ/leaf-grouping-ZZ
        final LeafSchemaNode leafZZinGZZ = (LeafSchemaNode) gzz.getDataChildByName(
            QName.create(namespace, "leaf-grouping-ZZ"));
        assertFalse(leafZZinGZZ.isAddedByUses());
        assertEquals(QName.create(expectedModule, "leaf-grouping-ZZ"), leafZZinGZZ.getQName());

        // TEST getOriginal from grouping-U
        assertEquals(gv.getDataChildByName(QName.create(namespace, "leaf-grouping-V")),
            extractOriginal(gu.getDataChildByName(QName.create(namespace, "leaf-grouping-V"))));
        containerGroupingV = (ContainerSchemaNode) gu.getDataChildByName(
            QName.create(namespace, "container-grouping-V"));
        assertEquals(gv.getDataChildByName(QName.create(namespace, "container-grouping-V")),
            extractOriginal(containerGroupingV));
        assertEquals(gx.getDataChildByName(QName.create(namespace, "leaf-grouping-X")),
            extractOriginal(containerGroupingV.getDataChildByName(QName.create(namespace, "leaf-grouping-X"))));
        assertEquals(gy.getDataChildByName(QName.create(namespace, "leaf-grouping-Y")),
            extractOriginal(containerGroupingV.getDataChildByName(QName.create(namespace, "leaf-grouping-Y"))));

        assertEquals(gz.getDataChildByName(QName.create(namespace, "leaf-grouping-Z")),
            extractOriginal(gu.getDataChildByName(QName.create(namespace, "leaf-grouping-Z"))));
        assertEquals(gzz.getDataChildByName(QName.create(namespace, "leaf-grouping-ZZ")),
            extractOriginal(gu.getDataChildByName(QName.create(namespace, "leaf-grouping-ZZ"))));

        // TEST getOriginal from grouping-V
        assertEquals(gz.getDataChildByName(QName.create(namespace, "leaf-grouping-Z")),
            extractOriginal(gv.getDataChildByName(QName.create(namespace, "leaf-grouping-Z"))));
        assertEquals(gzz.getDataChildByName(QName.create(namespace, "leaf-grouping-ZZ")),
            extractOriginal(gv.getDataChildByName(QName.create(namespace, "leaf-grouping-ZZ"))));

        // TEST getOriginal from grouping-X
        assertEquals(gy.getDataChildByName(QName.create(namespace, "leaf-grouping-Y")),
            extractOriginal(gx.getDataChildByName(QName.create(namespace, "leaf-grouping-Y"))));
    }

    @Test
    public void testAddedByUsesLeafTypeQName() throws Exception {
        final EffectiveModelContext loadModules = TestUtils.parseYangSources("/added-by-uses-leaf-test");
        assertEquals(2, loadModules.getModules().size());
        foo = TestUtils.findModule(loadModules, "foo").get();
        final Module imp = TestUtils.findModule(loadModules, "import-module").get();

        final LeafSchemaNode leaf = (LeafSchemaNode)
            ((ContainerSchemaNode) foo.getDataChildByName(QName.create(foo.getQNameModule(), "my-container")))
            .getDataChildByName(QName.create(foo.getQNameModule(), "my-leaf"));

        TypeDefinition<?> impType = null;
        for (final TypeDefinition<?> typeDefinition : imp.getTypeDefinitions()) {
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
        for (DataSchemaNode child : node.getChildNodes()) {
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
        if (node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node)
                    .getChildNodes()) {
                assertIsAddedByUses(child, expected);
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) node).getCases()) {
                assertIsAddedByUses(caseNode, expected);
            }
        }
    }

    private static @Nullable SchemaNode extractOriginal(final SchemaNode node) {
        assertThat(node, instanceOf(DerivableSchemaNode.class));
        return ((DerivableSchemaNode) node).getOriginal().orElse(null);
    }
}
