/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT588Test {
    private static final String NS = "foo";
    private static final String REV = "2016-03-01";

    @Test
    void test() {
        final var context = YangParserTestUtils.parseYangResource("/yt588.yang");
        final var root = QName.create(NS, REV, "root");
        final var leafRef2 = QName.create(NS, REV, "leaf-ref-2");
        final var conGrp = QName.create(NS, REV, "con-grp");
        final var leafRef = QName.create(NS, REV, "leaf-ref");

        assertResolvedTypeDefinition(BinaryTypeDefinition.class,
            SchemaInferenceStack.ofDataTreePath(context, root, conGrp, leafRef),
            assertInstanceOf(LeafrefTypeDefinition.class,
                assertInstanceOf(LeafSchemaNode.class, context.findDataTreeChild(root, conGrp, leafRef).orElseThrow())
                    .getType()).getPathStatement());

        assertResolvedTypeDefinition(Int16TypeDefinition.class,
            SchemaInferenceStack.ofDataTreePath(context, root, leafRef2),
            assertInstanceOf(LeafrefTypeDefinition.class,
                assertInstanceOf(LeafSchemaNode.class, context.findDataTreeChild(root, leafRef2).orElseThrow())
                    .getType()).getPathStatement());
    }

    private static void assertResolvedTypeDefinition(final Class<? extends TypeDefinition<?>> expectedType,
            final SchemaInferenceStack stack, final PathExpression expression) {
        final var typed = assertInstanceOf(TypedDataSchemaNode.class, stack.resolvePathExpression(expression));
        assertInstanceOf(expectedType, typed.getType());
    }
}
