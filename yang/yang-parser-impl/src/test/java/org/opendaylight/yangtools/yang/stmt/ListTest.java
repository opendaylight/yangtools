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
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.DefaultReactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class ListTest {

    @Test
    public void listAndLeavesTest() throws ReactorException {
        final SchemaContext result = DefaultReactors.defaultReactor().newBuild()
                .addSource(sourceForResource("/list-test/list-test.yang"))
                .buildEffective();
        assertNotNull(result);

        final Module testModule = result.findModules("list-test").iterator().next();
        assertNotNull(testModule);

        final ListSchemaNode list = (ListSchemaNode) testModule.getDataChildByName(
            QName.create(testModule.getQNameModule(), "simple-list"));
        assertNotNull(list);

        assertTrue(list.isUserOrdered());
        assertTrue(list.isConfiguration());
        final List<QName> keys = list.getKeyDefinition();
        assertEquals(2, keys.size());

        assertEquals("key1", keys.get(0).getLocalName());
        assertEquals("key2", keys.get(1).getLocalName());

        assertEquals(1, list.getElementCountConstraint().get().getMinElements());
        assertEquals(10, list.getElementCountConstraint().get().getMaxElements());

        assertEquals(5, list.getChildNodes().size());

        LeafSchemaNode leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(),
            "key1"));
        assertNotNull(leaf);
        assertTrue(leaf.isMandatory());
        assertEquals("int32", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "key2"));
        assertNotNull(leaf);
        assertTrue(leaf.isMandatory());
        assertEquals("int16", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "old-leaf"));
        assertNotNull(leaf);
        assertFalse(leaf.isMandatory());
        assertEquals("string", leaf.getType().getQName().getLocalName());

        leaf = (LeafSchemaNode) list.getDataChildByName(QName.create(testModule.getQNameModule(), "young-leaf"));
        assertNotNull(leaf);
        assertFalse(leaf.isMandatory());
        assertEquals("young-leaf", leaf.getType().getQName().getLocalName());
        assertEquals(Optional.of("default-value"), leaf.getType().getDefaultValue());

        final LeafListSchemaNode leafList = (LeafListSchemaNode) list.getDataChildByName(
            QName.create(testModule.getQNameModule(), "list-of-leaves"));
        assertNotNull(leafList);
        assertTrue(leafList.isUserOrdered());
        assertEquals(2, leafList.getElementCountConstraint().get().getMinElements());
        assertEquals(20, leafList.getElementCountConstraint().get().getMaxElements());
        assertEquals("string", leafList.getType().getQName().getLocalName());
    }
}
