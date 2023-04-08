/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

class YinFileListStmtTest extends AbstractYinModulesTest {
    @Test
    void testListAndLeaves() {
        final var testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        final var list = assertInstanceOf(ListSchemaNode.class,
            testModule.findDataChildByName(
                QName.create(testModule.getQNameModule(), "modules"),
                QName.create(testModule.getQNameModule(), "module"))
            .orElseThrow());
        final var keys = list.getKeyDefinition();
        assertEquals(1, keys.size());
        assertEquals("name", keys.get(0).getLocalName());

        final var children = list.getChildNodes();
        assertEquals(4, children.size());

        final var childrenIterator = children.iterator();
        var leaf = assertInstanceOf(LeafSchemaNode.class, childrenIterator.next());
        assertEquals("name", leaf.getQName().getLocalName());
        assertEquals(Optional.of("Unique module instance name"), leaf.getDescription());
        assertEquals(BaseTypes.stringType(), leaf.getType());
        assertTrue(leaf.isMandatory());

        leaf = assertInstanceOf(LeafSchemaNode.class, childrenIterator.next());
        assertEquals("type", leaf.getQName().getLocalName());

        final var leafType = assertInstanceOf(IdentityrefTypeDefinition.class, leaf.getType());
        assertEquals("module-type", leafType.getIdentities().iterator().next().getQName().getLocalName());
        assertTrue(leaf.isMandatory());
    }
}
