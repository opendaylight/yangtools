/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
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
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYang("""
            module foo {
              namespace foo;
              prefix foo;
              yang-version 1.1;
              container one {
                choice two {
                  choice three {
                    leaf four {
                      type string;
                    }
                  }
                }
              }
              augment /one {
                list five;
              }
              augment /one/two/three/three {
                leaf six {
                  type string;
                }
              }
            }"""));
    }

    @AfterAll
    static void cleanup() {
        CONTEXT = null;
    }

    @Test
    void testEnterThroughChoice() {
        final var one = assertInstanceOf(ContainerContextNode.class, CONTEXT.getRoot().enterChild(stack, ONE));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var two = assertInstanceOf(ChoiceNodeContextNode.class, one.enterChild(FOUR, stack));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        final var three = assertInstanceOf(ChoiceNodeContextNode.class, two.enterChild(FOUR, stack));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        assertInstanceOf(LeafContextNode.class, three.enterChild(FOUR, stack));
        assertInstanceOf(LeafEffectiveStatement.class, stack.currentStatement());

        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, FOUR, FOUR), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterThroughAugment() {
        final var one = assertInstanceOf(ContainerContextNode.class, CONTEXT.getRoot().enterChild(stack, ONE));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var augment = assertInstanceOf(AugmentationContextNode.class, one.enterChild(FIVE, stack));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var five = assertInstanceOf(UnkeyedListMixinContextNode.class, augment.enterChild(FIVE, stack));
        assertInstanceOf(ListEffectiveStatement.class, stack.currentStatement());

        assertInstanceOf(UnkeyedListItemContextNode.class, five.enterChild(FIVE, stack));
        assertInstanceOf(ListEffectiveStatement.class, stack.currentStatement());

        assertEquals(Absolute.of(ONE, FIVE), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterThroughAugmentChoiceAugment() {
        final var one = assertInstanceOf(ContainerContextNode.class, CONTEXT.getRoot().enterChild(stack, ONE));
        assertInstanceOf(ContainerEffectiveStatement.class, stack.currentStatement());

        final var two = assertInstanceOf(ChoiceNodeContextNode.class, one.enterChild(SIX, stack));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        final var three = assertInstanceOf(ChoiceNodeContextNode.class, two.enterChild(SIX, stack));
        assertInstanceOf(ChoiceEffectiveStatement.class, stack.currentStatement());

        assertInstanceOf(LeafContextNode.class, three.enterChild(SIX, stack));
        assertInstanceOf(LeafEffectiveStatement.class, stack.currentStatement());

        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, SIX, SIX), stack.toSchemaNodeIdentifier());
    }

    @Test
    void testEnterChoicePath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.create(
            new NodeIdentifier(ONE),
            new NodeIdentifier(TWO),
            new NodeIdentifier(THREE),
            new NodeIdentifier(FOUR)))
            .orElseThrow();

        assertInstanceOf(LeafContextNode.class, result.node());
        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, FOUR, FOUR), result.stack().toSchemaNodeIdentifier());
    }

    @Test
    void testEnterAugmentPath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.create(
            new NodeIdentifier(ONE),
            new AugmentationIdentifier(Set.of(FIVE)),
            new NodeIdentifier(FIVE),
            new NodeIdentifier(FIVE)))
            .orElseThrow();

        assertInstanceOf(UnkeyedListItemContextNode.class, result.node());
        assertEquals(Absolute.of(ONE, FIVE), result.stack().toSchemaNodeIdentifier());
    }

    @Test
    void testEnterAugmentChoicePath() {
        final var result = CONTEXT.enterPath(YangInstanceIdentifier.create(
            new NodeIdentifier(ONE),
            new NodeIdentifier(TWO),
            new NodeIdentifier(THREE),
            new NodeIdentifier(SIX)))
            .orElseThrow();

        assertInstanceOf(LeafContextNode.class, result.node());
        assertEquals(Absolute.of(ONE, TWO, THREE, THREE, SIX, SIX), result.stack().toSchemaNodeIdentifier());
    }
}
