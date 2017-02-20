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
import static org.opendaylight.yangtools.yang.stmt.StmtTestUtils.sourceForResource;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;

public class ListTest {

    private static final StatementStreamSource LIST_MODULE = sourceForResource("/list-test/list-test.yang");

    @Test
    public void listAndLeavesTest() throws ReactorException {
        final CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(LIST_MODULE);

        final EffectiveSchemaContext result = reactor.buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModuleByName("list-test", null);
        assertNotNull(testModule);

        final ListSchemaNode list = (ListSchemaNode) testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "simple-list"));
        assertNotNull(list);

        assertTrue(list.isUserOrdered());
        assertTrue(list.isConfiguration());
        final List<QName> keys = list.getKeyDefinition();
        assertEquals(2, keys.size());

        assertEquals("key1", keys.get(0).getLocalName());
        assertEquals("key2", keys.get(1).getLocalName());

        assertEquals(1, list.getConstraints().getMinElements().intValue());
        assertEquals(10, list.getConstraints().getMaxElements().intValue());

        assertEquals(5, list.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "key1"));
        assertNotNull(leaf);
        assertTrue(leaf.getConstraints().isMandatory());
        assertEquals("int32", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "key2"));
        assertNotNull(leaf);
        assertTrue(leaf.getConstraints().isMandatory());
        assertEquals("int16", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "old-leaf"));
        assertNotNull(leaf);
        assertFalse(leaf.getConstraints().isMandatory());
        assertEquals("string", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "young-leaf"));
        assertNotNull(leaf);
        assertFalse(leaf.getConstraints().isMandatory());
        assertEquals("young-leaf", leaf.getType().getQName().getLocalName());
        assertEquals("default-value", leaf.getDefault());

        final LeafListSchemaNode leafList = (LeafListSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "list-of-leaves"));
        assertNotNull(leafList);
        assertTrue(leafList.getConstraints().isMandatory());
        assertTrue(leafList.isUserOrdered());
        assertEquals(2, leafList.getConstraints().getMinElements().intValue());
        assertEquals(20, leafList.getConstraints().getMaxElements().intValue());
        assertEquals("string", leafList.getType().getQName().getLocalName());
    }
}
