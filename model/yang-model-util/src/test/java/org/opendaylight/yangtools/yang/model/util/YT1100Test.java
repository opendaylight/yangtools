/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1100Test {
    @Test
    void testChoiceCaseRelativeLeafref() {
        final var context = YangParserTestUtils.parseYangResource("/yt1100.yang");
        final var module = context.findModule("yt1100").orElseThrow();
        final var qnm = module.getQNameModule();
        final var foo = QName.create(qnm, "foo");
        final var schedulerNode = QName.create(qnm, "scheduler-node");
        final var childSchedulerNodes = QName.create(qnm, "child-scheduler-nodes");
        final var name = QName.create(qnm, "name");
        final var leaf = assertInstanceOf(LeafSchemaNode.class,
            module.findDataTreeChild(foo, schedulerNode, childSchedulerNodes, name).orElseThrow());
        final var leafref = assertInstanceOf(LeafrefTypeDefinition.class, leaf.getType()).getPathStatement();

        final var resolvedLeafRef = assertInstanceOf(LeafSchemaNode.class,
            SchemaInferenceStack.ofDataTreePath(context, foo, schedulerNode, childSchedulerNodes, name)
                .resolvePathExpression(leafref));
        assertEquals(QName.create(qnm, "name"), resolvedLeafRef.getQName());
        assertInstanceOf(StringTypeDefinition.class, resolvedLeafRef.getType());
    }
}
