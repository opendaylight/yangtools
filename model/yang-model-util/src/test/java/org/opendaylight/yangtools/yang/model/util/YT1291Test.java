/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1291Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName INPUT = QName.create("foo", "input");
    private static final QName OUTPUT = QName.create("foo", "output");

    private static EffectiveModelContext context;

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/yt1291.yang");
    }

    @Test
    void testRpcIndexing() {
        final SchemaTreeEffectiveStatement<?> foo = Iterables.getOnlyElement(context.getModuleStatements().values())
                .findSchemaTreeNode(FOO).orElseThrow();
        assertThat(foo, instanceOf(RpcEffectiveStatement.class));
        final RpcEffectiveStatement rpc = (RpcEffectiveStatement) foo;

        assertThat(rpc.findDataTreeNode(INPUT).orElseThrow(), instanceOf(InputEffectiveStatement.class));
        assertThat(rpc.findDataTreeNode(OUTPUT).orElseThrow(), instanceOf(OutputEffectiveStatement.class));
    }

    @Test
    void testEnterDataTree() {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context, Absolute.of(FOO));
        assertThat(stack.enterDataTree(INPUT), instanceOf(InputEffectiveStatement.class));
        stack.exit();
        assertThat(stack.enterDataTree(OUTPUT), instanceOf(OutputEffectiveStatement.class));
    }
}
