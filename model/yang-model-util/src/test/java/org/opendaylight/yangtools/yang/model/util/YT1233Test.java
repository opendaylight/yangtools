/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1233Test {
    private static EffectiveModelContext context;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(context);

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/yt1233.yang");
    }

    @Test
    void testExitToDataTree() {
        final DataTreeEffectiveStatement<?> foo = stack.enterDataTree(QName.create("foo", "foo"));
        assertSame(foo, stack.exitToDataTree());
        assertTrue(stack.isEmpty());
        assertSame(foo, stack.enterDataTree(foo.argument()));
    }

    @Test
    void testExitToGrouping() {
        final GroupingEffectiveStatement baz = stack.enterGrouping(QName.create("foo", "baz"));
        assertTrue(stack.inGrouping());
        final DataTreeEffectiveStatement<?> xyzzy = stack.enterDataTree(QName.create("foo", "xyzzy"));
        assertSame(xyzzy, stack.exitToDataTree());
        assertSame(baz, stack.currentStatement());
        assertSame(xyzzy, stack.enterDataTree(xyzzy.argument()));
    }

    @Test
    void testEmptyExitToDataTree() {
        assertThrows(NoSuchElementException.class, stack::exitToDataTree);
    }

    @Test
    void testSchemaExitToDataTree() {
        stack.enterSchemaTree(QName.create("foo", "bar"));
        final IllegalStateException ex = assertThrows(IllegalStateException.class, stack::exitToDataTree);
        assertThat(ex.getMessage(), startsWith("Unexpected current "));
    }
}
