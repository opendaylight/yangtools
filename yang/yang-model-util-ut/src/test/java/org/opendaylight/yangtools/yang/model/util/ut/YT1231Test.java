/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.ut;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1231Test {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");
    private static final QName BAZ = QName.create("foo", "baz");
    private static final QName XYZZY = QName.create("foo", "xyzzy");

    @Test
    public void testEnterDataTree() {
        final EffectiveModelContext context = YangParserTestUtils.parseYangResource("/yt1231.yang");
        final SchemaInferenceStack stack = new SchemaInferenceStack(context);

        // Trivial
        assertThat(stack.enterDataTree(FOO), instanceOf(ContainerEffectiveStatement.class));
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
}
