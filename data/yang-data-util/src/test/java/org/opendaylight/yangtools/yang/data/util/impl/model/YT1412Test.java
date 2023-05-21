/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1412Test {
    private static final QNameModule MODULE = QNameModule.create(XMLNamespace.of("foo"));
    private static final QName ONE = QName.create(MODULE, "one");
    private static final QName TWO = QName.create(MODULE, "two");
    private static final QName THREE = QName.create(MODULE, "three");
    private static final QName FOUR = QName.create(MODULE, "four");
    private static final QName FIVE = QName.create(MODULE, "five");
    private static final QName SIX = QName.create(MODULE, "six");

    private static DataSchemaContextTree CONTEXT;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(CONTEXT.getEffectiveModelContext());

    @BeforeAll
    static void init() {
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYangResource("/yt1412.yang"));
    }

    @AfterAll
    static void cleanup() {
        CONTEXT = null;
    }

    @Test
    void testEnterThroughChoice() {
        final var one = assertInstanceOf(ContainerContextNode.class, CONTEXT.getRoot().enterChild(stack, ONE));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var two = assertInstanceOf(ChoiceContextNode.class, one.enterChild(stack, FOUR));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        final var three = assertInstanceOf(ChoiceContextNode.class, two.enterChild(stack, FOUR));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        assertInstanceOf(LeafContextNode.class, three.enterChild(stack, FOUR));
        assertInstanceOf(LeafEffectiveStatement.class, stack.currentStatement());

        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, FOUR, FOUR), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterThroughAugment() {
        final var one = assertInstanceOf(ContainerContextNode.class, CONTEXT.getRoot().enterChild(stack, ONE));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var five = assertInstanceOf(ListContextNode.class, one.enterChild(stack, FIVE));
        assertInstanceOf(ListEffectiveStatement.class, stack.currentStatement());

        assertInstanceOf(ListItemContextNode.class, five.enterChild(stack, FIVE));
        assertInstanceOf(ListEffectiveStatement.class, stack.currentStatement());

        assertEquals(Absolute.of(ONE, FIVE), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterThroughAugmentChoiceAugment() {
        final var one = assertInstanceOf(ContainerContextNode.class, CONTEXT.getRoot().enterChild(stack, ONE));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var two = assertInstanceOf(ChoiceContextNode.class, one.enterChild(stack, SIX));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        final var three = assertInstanceOf(ChoiceContextNode.class, two.enterChild(stack, SIX));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        assertInstanceOf(LeafContextNode.class, three.enterChild(stack, SIX));
        assertInstanceOf(LeafEffectiveStatement.class, stack.currentStatement());

        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, SIX, SIX), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterChoicePath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.of(ONE, TWO, THREE, FOUR)).orElseThrow();

        assertInstanceOf(LeafContextNode.class, result.node());
        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, FOUR, FOUR), result.stack().toSchemaNodeIdentifier());
    }

    @Test
    void testEnterAugmentPath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.of(ONE, FIVE, FIVE)).orElseThrow();

        assertInstanceOf(ListItemContextNode.class, result.node());
        assertEquals(Absolute.of(ONE, FIVE), result.stack().toSchemaNodeIdentifier());
    }

    @Test
    void testEnterAugmentChoicePath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.of(ONE, TWO, THREE, SIX)).orElseThrow();

        assertInstanceOf(LeafContextNode.class, result.node());
        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, SIX, SIX), result.stack().toSchemaNodeIdentifier());
    }
}
