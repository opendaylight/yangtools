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
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public class YangParserSimpleTest {
    private static final QNameModule SN = QNameModule.create(URI.create("urn:opendaylight:simple-nodes"),
        Revision.of("2013-07-30"));
    private static final QName SN_NODES = QName.create(SN, "nodes");
    private static final SchemaPath SN_NODES_PATH = SchemaPath.create(true, SN_NODES);

    private SchemaContext context;
    private Module testModule;

    @Before
    public void init() throws Exception {
        context = TestUtils.loadModules(getClass().getResource("/simple-test").toURI());
        testModule = TestUtils.findModule(context, "simple-nodes").get();
    }

    @Test
    public void testParseAnyXml() {
        final AnyxmlSchemaNode data = (AnyxmlSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "data"));
        assertNotNull("'anyxml data not found'", data);
        assertFalse(data.equals(null));
        assertEquals("RegularAnyxmlEffectiveStatement{qname=(urn:opendaylight:simple-nodes?revision=2013-07-30)data, "
                + "path=AbsoluteSchemaPath{path=[(urn:opendaylight:simple-nodes?revision=2013-07-30)data]}}",
                data.toString());

        // test SchemaNode args
        assertEquals(QName.create(SN, "data"), data.getQName());
        assertEquals(Optional.of("anyxml desc"), data.getDescription());
        assertEquals(Optional.of("data ref"), data.getReference());
        assertEquals(Status.OBSOLETE, data.getStatus());
        assertEquals(0, data.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(data.isAugmenting());
        assertFalse(data.isConfiguration());

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
    public void testParseAnyData() {
        final AnydataSchemaNode anydata = (AnydataSchemaNode) testModule.findDataChildByName(
                QName.create(testModule.getQNameModule(), "data2")).orElse(null);

        assertNotNull("'anydata data not found'", anydata);
        assertEquals("RegularAnydataEffectiveStatement{qname=(urn:opendaylight:simple-nodes?revision=2013-07-30)data2, "
                        + "path=AbsoluteSchemaPath{path=[(urn:opendaylight:simple-nodes?revision=2013-07-30)data2]}}",
                anydata.toString());

        // test SchemaNode args
        assertEquals(QName.create(SN, "data2"), anydata.getQName());
        assertEquals(Optional.of("anydata desc"), anydata.getDescription());
        assertEquals(Optional.of("data ref"), anydata.getReference());
        assertEquals(Status.OBSOLETE, anydata.getStatus());
        assertEquals(0, anydata.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(anydata.isAugmenting());
        assertFalse(anydata.isConfiguration());

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
    public void testParseContainer() {
        final ContainerSchemaNode nodes = (ContainerSchemaNode) testModule
                .getDataChildByName(QName.create(testModule.getQNameModule(), "nodes"));
        // test SchemaNode args
        assertEquals(SN_NODES, nodes.getQName());
        assertEquals(SN_NODES_PATH, nodes.getPath());
        assertEquals(Optional.of("nodes collection"), nodes.getDescription());
        assertEquals(Optional.of("nodes ref"), nodes.getReference());
        assertEquals(Status.CURRENT, nodes.getStatus());
        assertEquals(0, nodes.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(nodes.isAugmenting());
        assertFalse(nodes.isConfiguration());

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
        assertEquals(SN_NODES_PATH.createChild(QName.create(SN, "nodes-type")), nodesType.getPath());
        assertFalse(nodesType.getDescription().isPresent());
        assertFalse(nodesType.getReference().isPresent());
        assertEquals(Status.CURRENT, nodesType.getStatus());
        assertEquals(0, nodesType.getUnknownSchemaNodes().size());

        // child nodes
        // total size = 8: defined 6, inserted by uses 2
        assertEquals(8, nodes.getChildNodes().size());
        final LeafListSchemaNode added = (LeafListSchemaNode)nodes.getDataChildByName(QName.create(
            testModule.getQNameModule(), "added"));
        assertEquals(createPath("nodes", "added"), added.getPath());
        assertEquals(createPath("mytype"), added.getType().getPath());

        final ListSchemaNode links = (ListSchemaNode) nodes.getDataChildByName(QName.create(
            testModule.getQNameModule(), "links"));
        assertFalse(links.isUserOrdered());

        final Collection<? extends GroupingDefinition> groupings = nodes.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition nodeGroup = groupings.iterator().next();
        final QName groupQName = QName.create(SN, "node-group");
        assertEquals(groupQName, nodeGroup.getQName());
        final SchemaPath nodeGroupPath = SN_NODES_PATH.createChild(groupQName);
        assertEquals(nodeGroupPath, nodeGroup.getPath());

        final Collection<? extends UsesNode> uses = nodes.getUses();
        assertEquals(1, uses.size());
        final UsesNode use = uses.iterator().next();
        assertEquals(nodeGroup, use.getSourceGrouping());
    }


    private static final URI NS = URI.create("urn:opendaylight:simple-nodes");

    private static SchemaPath createPath(final String... names) {
        final Revision rev = Revision.of("2013-07-30");
        final List<QName> path = new ArrayList<>();
        for (final String name : names) {
            path.add(QName.create(NS, rev, name));
        }
        return SchemaPath.create(path, true);
    }

}
