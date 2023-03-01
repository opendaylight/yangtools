/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1127Test {
    private static EffectiveModelContext context;

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/yt1127.yang");
    }

    @AfterAll
    static void afterClass() {
        context = null;
    }

    @Test
    void testGroupingLeafRef() {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(QName.create("foo", "grp"));
        final SchemaTreeEffectiveStatement<?> leaf1 = stack.enterSchemaTree(QName.create("foo", "leaf1"));
        assertThat(leaf1, instanceOf(LeafSchemaNode.class));
        final TypeDefinition<?> type = ((LeafSchemaNode) leaf1).getType();
        assertThat(type, instanceOf(LeafrefTypeDefinition.class));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stack.resolveLeafref((LeafrefTypeDefinition) type));
        assertThat(ex.getMessage(), startsWith("Illegal parent access in YangLocationPath"));
        final Throwable cause = ex.getCause();
        assertThat(cause, instanceOf(IllegalStateException.class));
        assertEquals("Unexpected current EmptyGroupingEffectiveStatement{argument=(foo)grp}", cause.getMessage());
    }

    @Test
    void testContainerLeafRef() {
        final SchemaInferenceStack stack = SchemaInferenceStack.ofDataTreePath(context,
                QName.create("foo", "cont"), QName.create("foo", "leaf2"));

        final EffectiveStatement<?, ?> leaf2 = stack.currentStatement();
        assertThat(leaf2, instanceOf(LeafSchemaNode.class));
        final TypeDefinition<?> type = ((LeafSchemaNode) leaf2).getType();
        assertThat(type, instanceOf(LeafrefTypeDefinition.class));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> stack.resolveLeafref((LeafrefTypeDefinition) type));
        assertThat(ex.getMessage(), startsWith("Illegal parent access in YangLocationPath"));
        assertThat(ex.getCause(), instanceOf(NoSuchElementException.class));
    }
}
