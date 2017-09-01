/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.TestUtils;
import org.xml.sax.SAXException;

public class YinFileGroupingStmtTest {

    private SchemaContext context;

    @Before
    public void init() throws ReactorException, SAXException, IOException, URISyntaxException {
        context = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, context.getModules().size());
    }

    @Test
    public void testGrouping() throws URISyntaxException {
        final Module testModule = TestUtils.findModule(context, "config").get();
        assertNotNull(testModule);

        final Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        final Iterator<GroupingDefinition> groupingsIterator = groupings.iterator();
        final GroupingDefinition grouping = groupingsIterator.next();
        assertEquals("service-ref", grouping.getQName().getLocalName());
        assertEquals("Type of references to a particular service instance. This type\n" +
                "can be used when defining module configuration to refer to a\n" +
                "particular service instance. Containers using this grouping\n" +
                "should not define anything else. The run-time implementation\n" +
                "is expected to inject a reference to the service as the value\n" +
                "of the container.", grouping.getDescription());

        final Collection<DataSchemaNode> children = grouping.getChildNodes();
        assertEquals(2, children.size());

        final LeafSchemaNode leaf1 = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                testModule.getQNameModule(), "type"));
        assertNotNull(leaf1);
        assertTrue(leaf1.getConstraints().isMandatory());

        final LeafSchemaNode leaf2 = (LeafSchemaNode) grouping.getDataChildByName(QName.create(
                testModule.getQNameModule(), "name"));
        assertNotNull(leaf2);
        assertTrue(leaf2.getConstraints().isMandatory());
    }
}
