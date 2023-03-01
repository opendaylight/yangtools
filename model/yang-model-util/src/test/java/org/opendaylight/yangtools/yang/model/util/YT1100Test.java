/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1100Test {
    @Test
    void testChoiceCaseRelativeLeafref() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource("/yt1100.yang");
        final Module module = context.findModule("yt1100").orElseThrow();
        final QNameModule qnm = module.getQNameModule();
        final QName foo = QName.create(qnm, "foo");
        final QName schedulerNode = QName.create(qnm, "scheduler-node");
        final QName childSchedulerNodes = QName.create(qnm, "child-scheduler-nodes");
        final QName name = QName.create(qnm, "name");
        final DataSchemaNode leaf = module.findDataTreeChild(foo, schedulerNode, childSchedulerNodes, name)
                .orElseThrow();
        assertThat(leaf, instanceOf(LeafSchemaNode.class));

        final TypeDefinition<?> type = ((LeafSchemaNode) leaf).getType();
        assertThat(type, instanceOf(LeafrefTypeDefinition.class));
        final PathExpression leafref = ((LeafrefTypeDefinition) type).getPathStatement();

        final EffectiveStatement<?, ?> resolvedLeafRef = SchemaInferenceStack.ofDataTreePath(
                context, foo, schedulerNode, childSchedulerNodes, name).resolvePathExpression(leafref);
        assertThat(resolvedLeafRef, instanceOf(LeafSchemaNode.class));
        final LeafSchemaNode targetLeaf = (LeafSchemaNode) resolvedLeafRef;
        assertEquals(QName.create(qnm, "name"), targetLeaf.getQName());
        assertThat(targetLeaf.getType(), instanceOf(StringTypeDefinition.class));
    }
}
