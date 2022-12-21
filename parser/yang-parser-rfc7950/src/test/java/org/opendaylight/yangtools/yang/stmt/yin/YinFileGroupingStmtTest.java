/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

class YinFileGroupingStmtTest extends AbstractYinModulesTest {
    @Test
    void testGrouping() {
        final Module testModule = context.findModules("config").iterator().next();
        assertNotNull(testModule);

        final Collection<? extends GroupingDefinition> groupings = testModule.getGroupings();
        assertEquals(1, groupings.size());

        final Iterator<? extends GroupingDefinition> groupingsIterator = groupings.iterator();
        final GroupingDefinition grouping = groupingsIterator.next();
        assertEquals("service-ref", grouping.getQName().getLocalName());
        assertEquals(Optional.of("Type of references to a particular service instance. This type\n"
            + "can be used when defining module configuration to refer to a\n"
            + "particular service instance. Containers using this grouping\n"
            + "should not define anything else. The run-time implementation\n"
            + "is expected to inject a reference to the service as the value\n"
            + "of the container."), grouping.getDescription());

        final Collection<? extends DataSchemaNode> children = grouping.getChildNodes();
        assertEquals(2, children.size());

        final LeafSchemaNode leaf1 = (LeafSchemaNode) grouping.findDataChildByName(QName.create(
            testModule.getQNameModule(), "type")).get();
        assertTrue(leaf1.isMandatory());

        final LeafSchemaNode leaf2 = (LeafSchemaNode) grouping.findDataChildByName(QName.create(
            testModule.getQNameModule(), "name")).get();
        assertTrue(leaf2.isMandatory());
    }
}
