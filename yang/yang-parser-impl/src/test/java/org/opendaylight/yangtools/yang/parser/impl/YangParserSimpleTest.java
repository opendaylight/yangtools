/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class YangParserSimpleTest {
    private final URI snNS = URI.create("urn:opendaylight:simple-nodes");
    private Date snRev;
    private final String snPref = "sn";

    private final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Set<Module> modules;

    //TODO: uncomment and run this test once commits are merged
    @Before
    public void init() throws Exception {
        snRev = simpleDateFormat.parse("2013-07-30");
        modules = TestUtils.loadModules(getClass().getResource("/simple-test").toURI());
    }

    @Test
    public void testParseAnyXml() {
        Module testModule = TestUtils.findModule(modules, "simple-nodes");
        AnyXmlSchemaNode data = (AnyXmlSchemaNode) testModule.getDataChildByName("data");
        assertNotNull("'anyxml data not found'", data);

        // test SchemaNode args
        QName qname = data.getQName();
        assertEquals("data", qname.getLocalName());
        assertEquals(snNS, qname.getNamespace());
        assertEquals(snRev, qname.getRevision());
        assertEquals("anyxml desc", data.getDescription());
        assertEquals("data ref", data.getReference());
        assertEquals(Status.OBSOLETE, data.getStatus());
        assertEquals(0, data.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(data.isAugmenting());
        assertFalse(data.isConfiguration());
        ConstraintDefinition constraints = data.getConstraints();
        assertEquals("class != 'wheel'", constraints.getWhenCondition().toString());
        Set<MustDefinition> mustConstraints = constraints.getMustConstraints();
        assertEquals(2, constraints.getMustConstraints().size());

        String must1 = "\"ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)\"";
        String errMsg1 = "An ethernet MTU must be 1500";
        String must2 = "\"ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)\"";
        String errMsg2 = "An atm MTU must be  64 .. 17966";

        boolean found1 = false;
        boolean found2 = false;
        for (MustDefinition must : mustConstraints) {
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
    public void testParseContainer() {
        Module test = TestUtils.findModule(modules, "simple-nodes");

        ContainerSchemaNode nodes = (ContainerSchemaNode) test.getDataChildByName("nodes");
        // test SchemaNode args
        QName expectedQName = QName.create(snNS, snRev, "nodes");
        assertEquals(expectedQName, nodes.getQName());
        SchemaPath expectedPath = TestUtils.createPath(true, snNS, snRev, snPref, "nodes");
        assertEquals(expectedPath, nodes.getPath());
        assertEquals("nodes collection", nodes.getDescription());
        assertEquals("nodes ref", nodes.getReference());
        assertEquals(Status.CURRENT, nodes.getStatus());
        assertEquals(0, nodes.getUnknownSchemaNodes().size());
        // test DataSchemaNode args
        assertFalse(nodes.isAugmenting());
        assertFalse(nodes.isConfiguration());

        // constraints
        ConstraintDefinition constraints = nodes.getConstraints();
        assertEquals("class != 'wheel'", constraints.getWhenCondition().toString());
        Set<MustDefinition> mustConstraints = constraints.getMustConstraints();
        assertEquals(2, constraints.getMustConstraints().size());

        String must1 = "\"ifType != 'atm' or (ifType = 'atm' and ifMTU <= 17966 and ifMTU >= 64)\"";
        String errMsg1 = "An atm MTU must be  64 .. 17966";
        String must2 = "ifId != 0";

        boolean found1 = false;
        boolean found2 = false;
        for (MustDefinition must : mustConstraints) {
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
        Set<TypeDefinition<?>> typedefs = nodes.getTypeDefinitions();
        assertEquals(1, typedefs.size());
        TypeDefinition<?> nodesType = typedefs.iterator().next();
        QName typedefQName = QName.create(snNS, snRev, "nodes-type");
        assertEquals(typedefQName, nodesType.getQName());
        SchemaPath nodesTypePath = TestUtils.createPath(true, snNS, snRev, snPref, "nodes", "nodes-type");
        assertEquals(nodesTypePath, nodesType.getPath());
        assertTrue(nodesType.getDescription().isEmpty());
        assertTrue(nodesType.getReference().isEmpty());
        assertEquals(Status.CURRENT, nodesType.getStatus());
        assertEquals(0, nodesType.getUnknownSchemaNodes().size());

        // child nodes
        // total size = 8: defined 6, inserted by uses 2
        assertEquals(8, nodes.getChildNodes().size());
        LeafListSchemaNode added = (LeafListSchemaNode)nodes.getDataChildByName("added");
        assertEquals(createPath("nodes", "added"), added.getPath());
        assertEquals(createPath("mytype"), added.getType().getPath());

        ListSchemaNode links = (ListSchemaNode) nodes.getDataChildByName("links");
        assertFalse(links.isUserOrdered());

        Set<GroupingDefinition> groupings = nodes.getGroupings();
        assertEquals(1, groupings.size());
        GroupingDefinition nodeGroup = groupings.iterator().next();
        QName groupQName = QName.create(snNS, snRev, "node-group");
        assertEquals(groupQName, nodeGroup.getQName());
        SchemaPath nodeGroupPath = TestUtils.createPath(true, snNS, snRev, snPref, "nodes", "node-group");
        assertEquals(nodeGroupPath, nodeGroup.getPath());

        Set<UsesNode> uses = nodes.getUses();
        assertEquals(1, uses.size());
        UsesNode use = uses.iterator().next();
        assertEquals(nodeGroupPath, use.getGroupingPath());
    }


    private final URI ns = URI.create("urn:opendaylight:simple-nodes");
    private Date rev;
    private final String prefix = "sn";

    private SchemaPath createPath(final String... names) {
        try {
            rev = new SimpleDateFormat("yyyy-MM-dd").parse("2013-07-30");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<QName> path = new ArrayList<>();
        for (String name : names) {
            path.add(QName.create(ns, rev, name));
        }
        return SchemaPath.create(path, true);
    }

}
