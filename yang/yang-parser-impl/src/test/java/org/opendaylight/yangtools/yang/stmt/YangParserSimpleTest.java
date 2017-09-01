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

import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
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
    private static final QNameModule SN =
            QNameModule.create(URI.create("urn:opendaylight:simple-nodes"), QName.parseRevision("2013-07-30"));
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
        final AnyXmlSchemaNode data = (AnyXmlSchemaNode) testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "data"));
        assertNotNull("'anyxml data not found'", data);
        assertFalse(data.equals(null));
        assertEquals("AnyXmlEffectiveStatementImpl[qname=(urn:opendaylight:simple-nodes?revision=2013-07-30)data, " +
                "path=AbsoluteSchemaPath{path=[(urn:opendaylight:simple-nodes?revision=2013-07-30)data]}]", data.toString());

        // test SchemaNode args
        assertEquals(QName.create(SN, "data"), data.getQName());
        assertEquals("anyxml desc", data.getDescription());
        assertEquals("data ref", data.getReference());
        assertEquals(Status.OBSOLETE, data.getStatus());
        assertEquals(0, data.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(data.isAugmenting());
        assertFalse(data.isConfiguration());
        final ConstraintDefinition constraints = data.getConstraints();
        assertEquals("class != 'wheel'", constraints.getWhenCondition().toString());
        final Set<MustDefinition> mustConstraints = constraints.getMustConstraints();
        assertEquals(2, constraints.getMustConstraints().size());

        final String must1 = "ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)";
        final String errMsg1 = "An ethernet MTU must be 1500";
        final String must2 = "ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)";
        final String errMsg2 = "An atm MTU must be  64 .. 17966";

        boolean found1 = false;
        boolean found2 = false;
        for (final MustDefinition must : mustConstraints) {
            if (must1.equals(must.toString())) {
                found1 = true;
                assertEquals(errMsg1, must.getErrorMessage());
            } else if (must2.equals(must.toString())) {
                found2 = true;
                assertEquals(errMsg2, must.getErrorMessage());
                assertEquals("anyxml data error-app-tag", must.getErrorAppTag());
                assertEquals("an error occured in data", must.getDescription());
                assertEquals("data must ref", must.getReference());
            }
        }
        assertTrue(found1);
        assertTrue(found2);

        assertTrue(constraints.isMandatory());
        assertNull(constraints.getMinElements());
        assertNull(constraints.getMaxElements());
    }

    @Test
    public void testParseContainer() throws ParseException {
        final ContainerSchemaNode nodes = (ContainerSchemaNode) testModule
                .getDataChildByName(QName.create(testModule.getQNameModule(), "nodes"));
        // test SchemaNode args
        assertEquals(SN_NODES, nodes.getQName());
        assertEquals(SN_NODES_PATH, nodes.getPath());
        assertEquals("nodes collection", nodes.getDescription());
        assertEquals("nodes ref", nodes.getReference());
        assertEquals(Status.CURRENT, nodes.getStatus());
        assertEquals(0, nodes.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(nodes.isAugmenting());
        assertFalse(nodes.isConfiguration());

        // constraints
        final ConstraintDefinition constraints = nodes.getConstraints();
        assertEquals("class != 'wheel'", constraints.getWhenCondition().toString());
        final Set<MustDefinition> mustConstraints = constraints.getMustConstraints();
        assertEquals(2, constraints.getMustConstraints().size());

        final String must1 = "ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)";
        final String errMsg1 = "An atm MTU must be  64 .. 17966";
        final String must2 = "ifId != 0";

        boolean found1 = false;
        boolean found2 = false;
        for (final MustDefinition must : mustConstraints) {
            if (must1.equals(must.toString())) {
                found1 = true;
                assertEquals(errMsg1, must.getErrorMessage());
            } else if (must2.equals(must.toString())) {
                found2 = true;
                assertNull(must.getErrorMessage());
                assertNull(must.getErrorAppTag());
                assertNull(must.getDescription());
                assertNull(must.getReference());
            }
        }
        assertTrue(found1);
        assertTrue(found2);

        assertFalse(constraints.isMandatory());
        assertNull(constraints.getMinElements());
        assertNull(constraints.getMaxElements());
        assertTrue(nodes.isPresenceContainer());

        // typedef
        final Set<TypeDefinition<?>> typedefs = nodes.getTypeDefinitions();
        assertEquals(1, typedefs.size());
        final TypeDefinition<?> nodesType = typedefs.iterator().next();
        final QName typedefQName = QName.create(SN, "nodes-type");
        assertEquals(typedefQName, nodesType.getQName());
        assertEquals(SN_NODES_PATH.createChild(QName.create(SN, "nodes-type")), nodesType.getPath());
        assertNull(nodesType.getDescription());
        assertNull(nodesType.getReference());
        assertEquals(Status.CURRENT, nodesType.getStatus());
        assertEquals(0, nodesType.getUnknownSchemaNodes().size());

        // child nodes
        // total size = 8: defined 6, inserted by uses 2
        assertEquals(8, nodes.getChildNodes().size());
        final LeafListSchemaNode added = (LeafListSchemaNode)nodes.getDataChildByName(QName.create(testModule.getQNameModule(), "added"));
        assertEquals(createPath("nodes", "added"), added.getPath());
        assertEquals(createPath("mytype"), added.getType().getPath());

        final ListSchemaNode links = (ListSchemaNode) nodes.getDataChildByName(QName.create(testModule.getQNameModule(), "links"));
        assertFalse(links.isUserOrdered());

        final Set<GroupingDefinition> groupings = nodes.getGroupings();
        assertEquals(1, groupings.size());
        final GroupingDefinition nodeGroup = groupings.iterator().next();
        final QName groupQName = QName.create(SN, "node-group");
        assertEquals(groupQName, nodeGroup.getQName());
        final SchemaPath nodeGroupPath = SN_NODES_PATH.createChild(groupQName);
        assertEquals(nodeGroupPath, nodeGroup.getPath());

        final Set<UsesNode> uses = nodes.getUses();
        assertEquals(1, uses.size());
        final UsesNode use = uses.iterator().next();
        assertEquals(nodeGroupPath, use.getGroupingPath());
    }


    private static final URI NS = URI.create("urn:opendaylight:simple-nodes");

    private static SchemaPath createPath(final String... names) throws ParseException {
        final Date rev = SimpleDateFormatUtil.getRevisionFormat().parse("2013-07-30");

        final List<QName> path = new ArrayList<>();
        for (final String name : names) {
            path.add(QName.create(NS, rev, name));
        }
        return SchemaPath.create(path, true);
    }

}
