/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ListTest {

    private static final YangStatementSourceImpl LIST_MODULE = new YangStatementSourceImpl("/list-test/list-test.yang",
            false);

    @Test
    public void listAndLeavesTest() throws ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        StmtTestUtils.addSources(reactor, LIST_MODULE);

        EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        Module testModule = result.findModuleByName("list-test", null);
        assertNotNull(testModule);

        ListSchemaNode list = (ListSchemaNode) testModule.getDataChildByName("simple-list");
        assertNotNull(list);

        assertTrue(list.isUserOrdered());
        assertTrue(list.isConfiguration());
        List<QName> keys = list.getKeyDefinition();
        assertEquals(2, keys.size());

        assertEquals("key1", keys.get(0).getLocalName());
        assertEquals("key2", keys.get(1).getLocalName());

        assertEquals(1, list.getConstraints().getMinElements().intValue());
        assertEquals(10, list.getConstraints().getMaxElements().intValue());

        assertEquals(5, list.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) list.getDataChildByName("key1");
        assertNotNull(leaf);
        assertTrue(leaf.getConstraints().isMandatory());
        assertEquals("int32", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName("key2");
        assertNotNull(leaf);
        assertTrue(leaf.getConstraints().isMandatory());
        assertEquals("int16", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName("old-leaf");
        assertNotNull(leaf);
        assertFalse(leaf.getConstraints().isMandatory());
        assertEquals("string", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName("young-leaf");
        assertNotNull(leaf);
        assertFalse(leaf.getConstraints().isMandatory());
        assertEquals("string", leaf.getType().getQName().getLocalName());
        assertEquals("default-value", leaf.getDefault());

        LeafListSchemaNode leafList = (LeafListSchemaNode) list.getDataChildByName("list-of-leaves");
        assertNotNull(leafList);
        assertFalse(leafList.getConstraints().isMandatory());
        assertTrue(leafList.isUserOrdered());
        assertEquals(2, leafList.getConstraints().getMinElements().intValue());
        assertEquals(20, leafList.getConstraints().getMaxElements().intValue());
        assertEquals("string", leafList.getType().getQName().getLocalName());
    }
}
