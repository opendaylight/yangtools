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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1231Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final QName XYZZY = QName.create("foo", "xyzzy");

    private static EffectiveModelContext CONTEXT;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(CONTEXT);

    @BeforeAll
    static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYangResource("/yt1231.yang");
    }

    @Test
    void testEnterDataTree() {
        // Trivial
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertSame(CONTEXT.getModuleStatement(FOO.getModule()), stack.currentModule());
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertEquals(Absolute.of(FOO, FOO), stack.toSchemaNodeIdentifier());
        stack.exit();

        // Has to cross four layers of choice/case
        assertThat(stack.enterDataTree(XYZZY), instanceOf(ContainerEffectiveStatement.class));
        assertEquals(Absolute.of(FOO, BAR, BAR, BAZ, BAZ, XYZZY), stack.toSchemaNodeIdentifier());

        stack.exit();
        assertThat(stack.enterSchemaTree(BAR), instanceOf(ChoiceEffectiveStatement.class));
        assertThat(stack.enterSchemaTree(BAR), instanceOf(CaseEffectiveStatement.class));
        assertEquals(Absolute.of(FOO, BAR, BAR), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterChoice() {
        // Simple
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertThat(stack.enterChoice(BAR), instanceOf(ChoiceEffectiveStatement.class));
        assertEquals(Absolute.of(FOO, BAR), stack.toSchemaNodeIdentifier());

        // Has to cross choice -> case -> choice
        assertThat(stack.enterChoice(BAZ), instanceOf(ChoiceEffectiveStatement.class));
        assertEquals(Absolute.of(FOO, BAR, BAR, BAZ), stack.toSchemaNodeIdentifier());

        // Now the same with just case -> choice
        stack.exit();
        assertThat(stack.enterSchemaTree(BAR), instanceOf(CaseEffectiveStatement.class));
        assertThat(stack.enterChoice(BAZ), instanceOf(ChoiceEffectiveStatement.class));
        assertEquals(Absolute.of(FOO, BAR, BAR, BAZ), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterChoiceToRootContainer() {
        assertEquals("Choice (foo)foo not present", assertEnterChoiceThrows(FOO));
    }

    @Test
    void testEnterChoiceToNestedContainer() {
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertEquals("Choice (foo)foo not present in schema parent (foo)foo", assertEnterChoiceThrows(FOO));
    }

    @Test
    void testEnterChoiceNonExistent() {
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
        assertEquals(Absolute.of(FOO), stack.toSchemaNodeIdentifier());
        assertThat(stack.enterSchemaTree(BAR), instanceOf(ChoiceEffectiveStatement.class));

        assertEquals("Choice (foo)foo not present in schema parent (foo)bar", assertEnterChoiceThrows(FOO));
        assertEquals("Choice (foo)bar not present in schema parent (foo)bar", assertEnterChoiceThrows(BAR));
    }

    private String assertEnterChoiceThrows(final QName nodeIdentifier) {
        return assertThrows(IllegalArgumentException.class, () -> stack.enterChoice(nodeIdentifier)).getMessage();
    }
}
