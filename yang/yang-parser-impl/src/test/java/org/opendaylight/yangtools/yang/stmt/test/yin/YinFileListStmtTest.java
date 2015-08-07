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
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.retest.TestUtils;

public class YinFileListStmtTest {

    private Set<Module> modules;

    @Before
    public void init() throws URISyntaxException, ReactorException {
        modules = TestUtils.loadYinModules(getClass().getResource("/semantic-statement-parser/yin/modules").toURI());
        assertEquals(9, modules.size());
    }

    @Test
    public void testListAndLeaves() throws URISyntaxException {
        Module testModule = TestUtils.findModule(modules, "config");
        assertNotNull(testModule);

        ContainerSchemaNode container = (ContainerSchemaNode) testModule.getDataChildByName("modules");
        assertNotNull(container);

        ListSchemaNode list = (ListSchemaNode) container.getDataChildByName("module");
        assertNotNull(list);
        List<QName> keys = list.getKeyDefinition();
        assertEquals(1, keys.size());
        assertEquals("name", keys.get(0).getLocalName());

        Collection<DataSchemaNode> children = list.getChildNodes();
        assertEquals(4, children.size());

        Iterator<DataSchemaNode> childrenIterator = children.iterator();
        LeafSchemaNode leaf = (LeafSchemaNode) childrenIterator.next();
        assertEquals("name", leaf.getQName().getLocalName());
        assertEquals("Unique module instance name", leaf.getDescription());
        assertTrue(leaf.getType().toString().startsWith("StringType"));
        assertTrue(leaf.getConstraints().isMandatory());

        leaf = (LeafSchemaNode) childrenIterator.next();
        assertEquals("type", leaf.getQName().getLocalName());
        assertEquals("identityref module-type", leaf.getType().toString());
        assertTrue(leaf.getConstraints().isMandatory());
    }

}
