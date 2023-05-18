/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1291Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName INPUT = QName.create("foo", "input");
    private static final QName OUTPUT = QName.create("foo", "output");

    private static EffectiveModelContext context;

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;
              rpc foo;
            }""");
    }

    @Test
    void testRpcIndexing() {
        final var rpc = assertInstanceOf(RpcEffectiveStatement.class,
            Iterables.getOnlyElement(context.getModuleStatements().values()).findSchemaTreeNode(FOO).orElseThrow());
        assertInstanceOf(InputEffectiveStatement.class, rpc.findDataTreeNode(INPUT).orElseThrow());
        assertInstanceOf(OutputEffectiveStatement.class, rpc.findDataTreeNode(OUTPUT).orElseThrow());
    }

    @Test
    void testEnterDataTree() {
        final var stack = SchemaInferenceStack.of(context, Absolute.of(FOO));
        assertInstanceOf(InputEffectiveStatement.class, stack.enterDataTree(INPUT));
        stack.exit();
        assertInstanceOf(OutputEffectiveStatement.class, stack.enterDataTree(OUTPUT));
    }
}
