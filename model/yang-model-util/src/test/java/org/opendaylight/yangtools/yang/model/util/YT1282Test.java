/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1282Test {
    private static final String YT1282_YANG = """
            module foo {
              prefix foo;
              namespace foo;
              typedef foo {
                type leafref {
                  path /bar;
                }
              }
              leaf bar {
                type int64;
              }
            }""";
    private static EffectiveModelContext context;

    private final SchemaInferenceStack stack = SchemaInferenceStack.of(context);

    @BeforeAll
    static void beforeClass() {
        context = YangParserTestUtils.parseYang(YT1282_YANG);
    }

    @Test
    void testResolveTypedef() {
        final TypeEffectiveStatement<?> type = stack.enterTypedef(QName.create("foo", "foo"))
                .findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
        assertFalse(stack.inInstantiatedContext());
        assertFalse(stack.inGrouping());

        final var bar = assertInstanceOf(LeafEffectiveStatement.class,
            stack.resolvePathExpression(
                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow()));
        assertEquals(QName.create("foo", "bar"), bar.argument());
    }
}
