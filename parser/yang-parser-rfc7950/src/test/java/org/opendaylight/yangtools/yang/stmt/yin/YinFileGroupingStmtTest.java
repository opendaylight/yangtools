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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

class YinFileGroupingStmtTest extends AbstractYinModulesTest {
    @Test
    void testGrouping() {
        final var testModule = context.findModules("config").iterator().next();
        final var groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        final var groupingsIterator = groupings.iterator();
        final var grouping = groupingsIterator.next();
        assertEquals("service-ref", grouping.getQName().getLocalName());
        assertEquals(Optional.of("""
            Type of references to a particular service instance. This type
            can be used when defining module configuration to refer to a
            particular service instance. Containers using this grouping
            should not define anything else. The run-time implementation
            is expected to inject a reference to the service as the value
            of the container."""), grouping.getDescription());

        final var children = grouping.getChildNodes();
        assertEquals(2, children.size());

        final var leaf1 = assertInstanceOf(LeafSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "type")));
        assertTrue(leaf1.isMandatory());

        final var leaf2 = assertInstanceOf(LeafSchemaNode.class,
            grouping.getDataChildByName(QName.create(testModule.getQNameModule(), "name")));
        assertTrue(leaf2.isMandatory());
    }
}
