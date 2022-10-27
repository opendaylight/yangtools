/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1412Test {
    private static final QNameModule MODULE = QNameModule.create(XMLNamespace.of("foo"));
    private static final QName ONE = QName.create(MODULE, "one");
    private static final QName TWO = QName.create(MODULE, "two");
    private static final QName THREE = QName.create(MODULE, "three");
    private static final QName FOUR = QName.create(MODULE, "four");
    private static final QName FIVE = QName.create(MODULE, "five");
    private static final QName SIX = QName.create(MODULE, "six");

    private static DataSchemaContextTree CONTEXT;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(CONTEXT.getEffectiveModelContext());

    @BeforeClass
    public static void init() {
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYangResource("/yt1412.yang"));
    }

    @AfterClass
    public static void cleanup() {
        CONTEXT = null;
    }

    @Test
    public void testEnterThroughChoice() {
        final var one = CONTEXT.getRoot().enterChild(stack, ONE);
        assertThat(one, instanceOf(ContainerContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var two = one.enterChild(FOUR, stack);
        assertThat(two, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var three = two.enterChild(FOUR, stack);
        assertThat(three, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var four = three.enterChild(FOUR, stack);
        assertThat(four, instanceOf(LeafContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(LeafEffectiveStatement.class));

        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, FOUR, FOUR), stack.toSchemaNodeIdentifier());
    }

    @Test
    public void testEnterThroughAugment() {
        final var one = CONTEXT.getRoot().enterChild(stack, ONE);
        assertThat(one, instanceOf(ContainerContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var five = one.enterChild(FIVE, stack);
        assertThat(five, instanceOf(UnkeyedListMixinContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ListEffectiveStatement.class));

        final var fiveItem = five.enterChild(FIVE, stack);
        assertThat(fiveItem, instanceOf(UnkeyedListItemContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ListEffectiveStatement.class));

        assertEquals(Absolute.of(ONE, FIVE), stack.toSchemaNodeIdentifier());
    }

    @Test
    public void testEnterThroughAugmentChoiceAugment() {
        final var one = CONTEXT.getRoot().enterChild(stack, ONE);
        assertThat(one, instanceOf(ContainerContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var two = one.enterChild(SIX, stack);
        assertThat(two, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var three = two.enterChild(SIX, stack);
        assertThat(three, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var six = three.enterChild(SIX, stack);
        assertThat(six, instanceOf(LeafContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(LeafEffectiveStatement.class));

        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, SIX, SIX), stack.toSchemaNodeIdentifier());
    }

    @Test
    public void testEnterChoicePath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.create(
            new NodeIdentifier(ONE),
            new NodeIdentifier(TWO),
            new NodeIdentifier(THREE),
            new NodeIdentifier(FOUR)))
            .orElseThrow();

        assertThat(result.node(), instanceOf(LeafContextNode.class));
        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, FOUR, FOUR), result.stack().toSchemaNodeIdentifier());
    }

    @Test
    public void testEnterAugmentPath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.create(
            new NodeIdentifier(ONE),
            new NodeIdentifier(FIVE),
            new NodeIdentifier(FIVE)))
            .orElseThrow();

        assertThat(result.node(), instanceOf(UnkeyedListItemContextNode.class));
        assertEquals(Absolute.of(ONE, FIVE), result.stack().toSchemaNodeIdentifier());
    }

    @Test
    public void testEnterAugmentChoicePath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.create(
            new NodeIdentifier(ONE),
            new NodeIdentifier(TWO),
            new NodeIdentifier(THREE),
            new NodeIdentifier(SIX)))
            .orElseThrow();

        assertThat(result.node(), instanceOf(LeafContextNode.class));
        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, SIX, SIX), result.stack().toSchemaNodeIdentifier());
    }
}
