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
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1283Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    private static EffectiveModelContext context;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(context);

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/yt1283.yang");
    }

    @Test
    void testResolveUnderCaseViaDataTree() {
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertResolve(stack.enterDataTree(FOO));
    }

    @Test
    void testResolveUnderCaseViaSchemaTree() {
        assertThat(stack.enterSchemaTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertThat(stack.enterSchemaTree(FOO), instanceOf(ChoiceEffectiveStatement.class));
        assertThat(stack.enterSchemaTree(FOO), instanceOf(CaseEffectiveStatement.class));
        assertResolve(stack.enterSchemaTree(FOO));
    }

    private void assertResolve(final EffectiveStatement<?, ?> foo) {
        assertThat(foo, instanceOf(LeafEffectiveStatement.class));

        final TypeEffectiveStatement<?> type = foo.findFirstEffectiveSubstatement(TypeEffectiveStatement.class)
            .orElseThrow();
        final EffectiveStatement<?, ?> bar = stack.resolvePathExpression(
            type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow());
        assertThat(bar, instanceOf(LeafEffectiveStatement.class));
        assertEquals(BAR, bar.argument());
    }
}
