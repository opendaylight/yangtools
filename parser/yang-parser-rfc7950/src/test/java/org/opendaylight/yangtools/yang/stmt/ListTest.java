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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.ElementCountMatcher;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;

class ListTest extends AbstractYangTest {
    @Test
    void listAndLeavesTest() {
        final var result = assertEffectiveModel("/list-test/list-test.yang");
        final var testModule = result.findModules("list-test").iterator().next();

        final var list = assertInstanceOf(ListSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "simple-list")));

        assertTrue(list.isUserOrdered());
        assertEquals(Optional.of(Boolean.TRUE), list.effectiveConfig());
        final var keys = list.getKeyDefinition();
        assertEquals(2, keys.size());

        assertEquals("key1", keys.get(0).getLocalName());
        assertEquals("key2", keys.get(1).getLocalName());

        var constraint = assertInstanceOf(ElementCountMatcher.InRange.class, list.elementCountMatcher());
        assertEquals(MinElementsArgument.of(1), constraint.atLeast());
        assertEquals(MaxElementsArgument.of(10), constraint.atMost());

        assertEquals(5, list.getChildNodes().size());

        var leaf = assertInstanceOf(LeafSchemaNode.class,
            list.getDataChildByName(QName.create(testModule.getQNameModule(), "key1")));
        assertTrue(leaf.isMandatory());
        assertEquals("int32", leaf.getType().getQName().getLocalName());

        leaf = assertInstanceOf(LeafSchemaNode.class,
            list.getDataChildByName(QName.create(testModule.getQNameModule(), "key2")));
        assertTrue(leaf.isMandatory());
        assertEquals("int16", leaf.getType().getQName().getLocalName());

        leaf = assertInstanceOf(LeafSchemaNode.class,
            list.getDataChildByName(QName.create(testModule.getQNameModule(), "old-leaf")));
        assertFalse(leaf.isMandatory());
        assertEquals("string", leaf.getType().getQName().getLocalName());

        leaf = assertInstanceOf(LeafSchemaNode.class,
            list.getDataChildByName(QName.create(testModule.getQNameModule(), "young-leaf")));
        assertFalse(leaf.isMandatory());
        assertEquals("young-leaf", leaf.getType().getQName().getLocalName());
        assertEquals(Optional.of("default-value"), leaf.getType().getDefaultValue());

        final var leafList = assertInstanceOf(LeafListSchemaNode.class,
            list.getDataChildByName(QName.create(testModule.getQNameModule(), "list-of-leaves")));
        assertTrue(leafList.isUserOrdered());

        constraint = assertInstanceOf(ElementCountMatcher.InRange.class, leafList.elementCountMatcher());
        assertEquals(MinElementsArgument.of(2), constraint.atLeast());
        assertEquals(MaxElementsArgument.of(20), constraint.atMost());
        assertEquals("string", leafList.getType().getQName().getLocalName());
    }
}
