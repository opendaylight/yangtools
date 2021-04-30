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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1282Test {
    private static EffectiveModelContext context;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(context);

    @BeforeClass
    public static void beforeClass() {
        context = YangParserTestUtils.parseYangResource("/yt1282.yang");
    }

    @Test
    public void testResolveTypedef() {
        final TypeEffectiveStatement<?> type = stack.enterTypedef(QName.create("foo", "foo"))
            .findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        assertFalse(stack.inInstantiatedContext());

        final EffectiveStatement<?, ?> bar = stack.resolvePathExpression(
            type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow());
        assertThat(bar, instanceOf(LeafEffectiveStatement.class));
        assertEquals(QName.create("foo", "bar"), bar.argument());
    }
}
