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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

class YangParserSimpleTest extends AbstractYangTest {
    private static final QNameModule SN = QNameModule.of("urn:opendaylight:simple-nodes", "2013-07-30");
    private static final QName SN_NODES = QName.create(SN, "nodes");

    private static Module MODULE;

    @BeforeAll
    static void beforeClass() throws Exception {
        MODULE = assertEffectiveModelDir("/simple-test").findModules("simple-nodes").iterator().next();
    }

    @Test
    void testParseAnyXml() {
        final AnyxmlSchemaNode data = (AnyxmlSchemaNode) MODULE.getDataChildByName(
            QName.create(MODULE.getQNameModule(), "data"));
        assertNotEquals(null, data);
        assertEquals(
            "RegularAnyxmlEffectiveStatement{argument=(urn:opendaylight:simple-nodes?revision=2013-07-30)data}",
            data.toString());

        // test SchemaNode args
        assertEquals(QName.create(SN, "data"), data.getQName());
        assertEquals(Optional.of("anyxml desc"), data.getDescription());
        assertEquals(Optional.of("data ref"), data.getReference());
        assertEquals(Status.OBSOLETE, data.getStatus());
        assertEquals(0, data.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(data.isAugmenting());
        assertEquals(Optional.of(Boolean.FALSE), data.effectiveConfig());

        assertTrue(data.isMandatory());
        assertEquals("class != 'wheel'", data.getWhenCondition().orElseThrow().toString());
        final Collection<? extends MustDefinition> mustConstraints = data.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final String must1 = "ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)";
        final String must2 = "ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)";

        boolean found1 = false;
        boolean found2 = false;
        for (final MustDefinition must : mustConstraints) {
            if (must1.equals(must.getXpath().toString())) {
                found1 = true;
                assertEquals(Optional.of("An ethernet MTU must be 1500"), must.getErrorMessage());
            } else if (must2.equals(must.getXpath().toString())) {
                found2 = true;
                assertEquals(Optional.of("An atm MTU must be  64 .. 17966"), must.getErrorMessage());
                assertEquals(Optional.of("anyxml data error-app-tag"), must.getErrorAppTag());
                assertEquals(Optional.of("an error occured in data"), must.getDescription());
                assertEquals(Optional.of("data must ref"), must.getReference());
            }
        }
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    void testParseAnyData() {
        final AnydataSchemaNode anydata = (AnydataSchemaNode) MODULE.findDataChildByName(
            QName.create(MODULE.getQNameModule(), "data2")).orElse(null);

        assertNotNull(anydata, "'anydata data not found'");
        assertEquals(
            "RegularAnydataEffectiveStatement{argument=(urn:opendaylight:simple-nodes?revision=2013-07-30)data2}",
            anydata.toString());

        // test SchemaNode args
        assertEquals(QName.create(SN, "data2"), anydata.getQName());
        assertEquals(Optional.of("anydata desc"), anydata.getDescription());
        assertEquals(Optional.of("data ref"), anydata.getReference());
        assertEquals(Status.OBSOLETE, anydata.getStatus());
        assertEquals(0, anydata.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(anydata.isAugmenting());
        assertEquals(Optional.of(Boolean.FALSE), anydata.effectiveConfig());

        assertTrue(anydata.isMandatory());
        assertTrue(anydata.getWhenCondition().isPresent());
        assertEquals("class != 'wheel'", anydata.getWhenCondition().orElseThrow().toString());
        final Collection<? extends MustDefinition> mustConstraints = anydata.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final String must1 = "ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)";
        final String must2 = "ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)";

        boolean found1 = false;
        boolean found2 = false;
        for (final MustDefinition must : mustConstraints) {
            if (must1.equals(must.getXpath().toString())) {
                found1 = true;
                assertEquals(Optional.of("An ethernet MTU must be 1500"), must.getErrorMessage());
            } else if (must2.equals(must.getXpath().toString())) {
                found2 = true;
                assertEquals(Optional.of("An atm MTU must be  64 .. 17966"), must.getErrorMessage());
                assertEquals(Optional.of("anydata data error-app-tag"), must.getErrorAppTag());
                assertEquals(Optional.of("an error occured in data"), must.getDescription());
                assertEquals(Optional.of("data must ref"), must.getReference());
            }
        }
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    void testParseContainer() {
        final ContainerSchemaNode nodes = (ContainerSchemaNode) MODULE
            .getDataChildByName(QName.create(MODULE.getQNameModule(), "nodes"));
        // test SchemaNode args
        assertEquals(SN_NODES, nodes.getQName());
        assertEquals(Optional.of("nodes collection"), nodes.getDescription());
        assertEquals(Optional.of("nodes ref"), nodes.getReference());
        assertEquals(Status.CURRENT, nodes.getStatus());
        assertEquals(0, nodes.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(nodes.isAugmenting());
        assertEquals(Optional.of(Boolean.FALSE), nodes.effectiveConfig());

        // constraints
        assertEquals("class != 'wheel'", nodes.getWhenCondition().orElseThrow().toString());
        final Collection<? extends MustDefinition> mustConstraints = nodes.getMustConstraints();
        assertEquals(2, mustConstraints.size());

        final String must1 = "ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)";
        final String errMsg1 = "An atm MTU must be  64 .. 17966";
        final String must2 = "ifId != 0";

        boolean found1 = false;
        boolean found2 = false;
        for (final MustDefinition must : mustConstraints) {
            if (must1.equals(must.getXpath().toString())) {
                found1 = true;
                assertEquals(Optional.of(errMsg1), must.getErrorMessage());
            } else if (must2.equals(must.getXpath().toString())) {
                found2 = true;
                assertFalse(must.getErrorMessage().isPresent());
                assertFalse(must.getErrorAppTag().isPresent());
                assertFalse(must.getDescription().isPresent());
                assertFalse(must.getReference().isPresent());
            }
        }
        assertTrue(found1);
        assertTrue(found2);

        assertTrue(nodes.isPresenceContainer());

        // typedef
        final Collection<? extends TypeDefinition<?>> typedefs = nodes.getTypeDefinitions();
        assertEquals(1, typedefs.size());
        final TypeDefinition<?> nodesType = typedefs.iterator().next();
        final QName typedefQName = QName.create(SN, "nodes-type");
        assertEquals(typedefQName, nodesType.getQName());
        assertFalse(nodesType.getDescription().isPresent());
        assertFalse(nodesType.getReference().isPresent());
        assertEquals(Status.CURRENT, nodesType.getStatus());
        assertEquals(0, nodesType.getUnknownSchemaNodes().size());

        // child nodes
        // total size = 8: defined 6, inserted by uses 2
        assertEquals(8, nodes.getChildNodes().size());
        final LeafListSchemaNode added = (LeafListSchemaNode) nodes.getDataChildByName(QName.create(
            MODULE.getQNameModule(), "added"));
        assertEquals(QName.create(SN, "added"), added.getQName());
        assertEquals(QName.create(SN, "mytype"), added.getType().getQName());

        final ListSchemaNode links = (ListSchemaNode) nodes.getDataChildByName(QName.create(
            MODULE.getQNameModule(), "links"));
        assertFalse(links.isUserOrdered());

        final Collection<? extends GroupingDefinition> groupings = nodes.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition nodeGroup = groupings.iterator().next();
        assertEquals(QName.create(SN, "node-group"), nodeGroup.getQName());

        final Collection<? extends UsesNode> uses = nodes.getUses();
        assertEquals(1, uses.size());
        final UsesNode use = uses.iterator().next();
        assertEquals(nodeGroup, use.getSourceGrouping());
    }
}
