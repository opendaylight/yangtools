/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YTXXXTest {
    private static final QNameModule MODULE = QNameModule.create(XMLNamespace.of("foo"));
    private static final QName ONE = QName.create(MODULE, "one");
    private static final QName TWO = QName.create(MODULE, "two");
    private static final QName THREE = QName.create(MODULE, "three");
    private static final QName FOUR = QName.create(MODULE, "four");
    private static final QName FIVE = QName.create(MODULE, "five");
    private static final QName SIX = QName.create(MODULE, "six");

    private static DataSchemaContextTree CONTEXT;

    @BeforeClass
    public static void init() {
        CONTEXT = DataSchemaContextTree.from(YangParserTestUtils.parseYangResource("/ytXXX.yang"));
    }

    @AfterClass
    public static void cleanup() {
        CONTEXT = null;
    }

    @Test
    public void testEnterChoice() {
        final var stack = SchemaInferenceStack.of(CONTEXT.getEffectiveModelContext());

        final var one = CONTEXT.getRoot().enterChild(stack, ONE);
        assertThat(one, instanceOf(ContainerContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ContainerEffectiveStatement.class));

        final var two = one.enterChild(FOUR, stack);
        assertThat(two, instanceOf(ChoiceNodeContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(ChoiceEffectiveStatement.class));

        final var four = two.enterChild(FOUR, stack);
        assertThat(four, instanceOf(LeafContextNode.class));
        assertThat(stack.currentStatement(), instanceOf(LeafEffectiveStatement.class));

        assertNotNull(four);

    }

}
