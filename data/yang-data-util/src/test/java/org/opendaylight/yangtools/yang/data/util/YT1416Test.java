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

import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1416Test {
    private static final QNameModule MODULE = QNameModule.create(XMLNamespace.of("foo"));
    private static final QName FOO = QName.create(MODULE, "foo");
    private static final QName BAR = QName.create(MODULE, "bar");
    private static final QName BAZ = QName.create(MODULE, "baz");
    private static final QName XYZZY = QName.create(MODULE, "xyzzy");
    private static final QName UINT = QName.create(MODULE, "uint");

    private static DataSchemaContextTree CONTEXT;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(CONTEXT.getEffectiveModelContext());

    @BeforeClass
    public static void init() {
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYangResource("/yt1416.yang"));
    }

    @AfterClass
    public static void cleanup() {
        CONTEXT = null;
    }

    @Test
    public void testContainerAugmentChoice() {
        final var foo = CONTEXT.getRoot().enterChild(stack, FOO);
        assertThat(foo, instanceOf(ContainerContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var augment = foo.enterChild(BAZ, stack);
        assertThat(augment, instanceOf(AugmentationContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var bar = augment.enterChild(BAZ, stack);
        assertThat(bar, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var baz = bar.enterChild(BAZ, stack);
        assertThat(baz, instanceOf(LeafContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(LeafEffectiveStatement.class));

        assertEquals(Absolute.of(FOO, BAR, BAZ, BAZ), stack.toSchemaNodeIdentifier());
    }

    @Test
    public void testContainerAugmentChoiceAugmentCase() {
        final var foo = CONTEXT.getRoot().enterChild(stack, FOO);
        assertThat(foo, instanceOf(ContainerContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var augment = foo.enterChild(UINT, stack);
        assertThat(augment, instanceOf(AugmentationContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var bar = augment.enterChild(UINT, stack);
        assertThat(bar, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var baz = bar.enterChild(UINT, stack);
        // FIXME: does not work: augment sees a choice here :(
        assertThat(baz, instanceOf(LeafContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(LeafEffectiveStatement.class));

        assertEquals(Absolute.of(FOO, BAR, BAZ, UINT), stack.toSchemaNodeIdentifier());
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
            new AugmentationIdentifier(Set.of(FIVE)),
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
