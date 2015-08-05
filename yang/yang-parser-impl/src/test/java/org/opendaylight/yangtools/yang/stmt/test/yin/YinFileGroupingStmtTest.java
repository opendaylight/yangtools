/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test.yin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileGroupingStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(10, modules.size());
    }

    @Test
    public void testGrouping() throws URISyntaxException {
        Module testModule = TestUtils.findModule(modules, "config");
        assertNotNull(testModule);

        Set<GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        Iterator<GroupingDefinition> groupingsIterator = groupings.iterator();
        GroupingDefinition grouping = groupingsIterator.next();
        assertEquals("service-ref", grouping.getQName().getLocalName());
        assertEquals("Type of references to a particular service instance. This type\n" +
                "can be used when defining module configuration to refer to a\n" +
                "particular service instance. Containers using this grouping\n" +
                "should not define anything else. The run-time implementation\n" +
                "is expected to inject a reference to the service as the value\n" +
                "of the container.", grouping.getDescription());

        Collection<DataSchemaNode> children = grouping.getChildNodes();
        assertEquals(2, children.size());

        LeafSchemaNode leaf1 = (LeafSchemaNode) grouping.getDataChildByName("type");
        assertNotNull(leaf1);
        assertTrue(leaf1.getConstraints().isMandatory());

        LeafSchemaNode leaf2 = (LeafSchemaNode) grouping.getDataChildByName("name");
        assertNotNull(leaf2);
        assertTrue(leaf2.getConstraints().isMandatory());
    }
}
